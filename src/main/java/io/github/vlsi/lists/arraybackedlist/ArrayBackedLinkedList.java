package io.github.vlsi.lists.arraybackedlist;

import java.io.*;
import java.util.*;

/**
 * A {@link List} and {@link Deque} implementation that stores elements in plain arrays while
 * maintaining logical links between them via integer indices (prev/next) instead of object
 * references. This design eliminates per-node object allocations and improves cache locality
 * compared to classic {@link java.util.LinkedList}.
 *
 * <p>Internally, three parallel arrays are used:
 * <ul>
 *   <li>elementData — holds the actual elements,</li>
 *   <li>next — index of the next element,</li>
 *   <li>prev — index of the previous element.</li>
 * </ul>
 * Free physical slots are recycled using a queue, allowing constant-time reuse without creating
 * new node objects.</p>
 *
 * <p><strong>Complexity characteristics</strong> (amortized unless stated otherwise):
 * <ul>
 *   <li>addFirst/addLast, offerFirst/offerLast, push: O(1)</li>
 *   <li>removeFirst/removeLast, pollFirst/pollLast, pop: O(1)</li>
 *   <li>add(int, E), remove(int): O(n)</li>
 *   <li>get(int), set(int, E): O(n)</li>
 *   <li>size(): O(1)</li>
 * </ul>
 * These mirror the typical characteristics of linked lists while benefiting from better memory
 * locality of arrays.</p>
 *
 * <p><strong>Fail-fast iterators:</strong> Iterators and list iterators are fail-fast: if the list is
 * structurally modified at any time after the iterator is created, in any way except through the
 * iterator's own methods, the iterator will throw {@link ConcurrentModificationException}.
 * The fail-fast behavior is best-effort and should not be relied upon for correctness.</p>
 *
 * <p><strong>Thread-safety:</strong> This implementation is not synchronized. If multiple threads
 * access an instance concurrently and at least one modifies it structurally, external
 * synchronization is required.</p>
 *
 * <p><strong>Serialization:</strong> The list is serializable. However, the reversed view returned by
 * {@link #reversed()} is a live O(1) view over the same data and is deliberately not serializable.</p>
 *
 * @param <E> the type of elements held in this list
 * @author Alternative Linked List Contributors
 * @since 1.0
 */
public class ArrayBackedLinkedList<E> extends AbstractSequentialList<E>
        implements List<E>, Deque<E>, Cloneable, Serializable {

    private static final int DEFAULT_CAPACITY = 10;

    transient Object[] elementData;
    // XOR links: for each index i, link[i] = enc(prev(i)) XOR enc(next(i)), where enc(-1)=0, enc(x>=0)=x+1
    transient int[] link;

    // Head/tail physical indices in arrays; -1 means empty
    private int head = -1;
    private int tail = -1;

    // Number of elements in the list
    private int size;

    // Number of positions that have ever been allocated [0..allocated)
    private int allocated;

    // Free indices available for reuse
    transient ArrayDeque<Integer> freeQueue = new ArrayDeque<>();

    // If true, this instance is a special view/copy that must not be serialized
    private boolean nonSerializableView = false;

    // Helper to mark views that must not be serialized (used by reversed view)
    void markNonSerializableView() {
        this.nonSerializableView = true;
    }

    public ArrayBackedLinkedList() {
        this.elementData = new Object[0];
        this.link = new int[0];
        this.allocated = 0;
    }

    public ArrayBackedLinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    // Capacity management (based on ArrayList grow rules)
    private void ensureCapacity(int minCapacity) {
        if (elementData.length < minCapacity) {
            grow(minCapacity);
        }
    }

    private void grow(int minCapacity) {
        int oldCap = elementData.length;
        int newCap = oldCap + (oldCap >> 1) + 1; // 1.5x + 1
        if (newCap < minCapacity) newCap = minCapacity;
        if (newCap < DEFAULT_CAPACITY) newCap = DEFAULT_CAPACITY;
        elementData = Arrays.copyOf(elementData, newCap);
        link = Arrays.copyOf(link, newCap);
    }

    private int allocateSlot() {
        if (!freeQueue.isEmpty()) {
            return freeQueue.removeFirst();
        }
        int idx = allocated++;
        if (idx >= elementData.length) {
            ensureCapacity(idx + 1);
        }
        return idx;
    }

    private int getNext(int idx, int prevIdx) {
        if (idx == -1) return -1;
        int e = link[idx] ^ prevIdx + 1;
        return e - 1;
    }

    private int getPrev(int idx, int nextIdx) {
        if (idx == -1) return -1;
        int e = link[idx] ^ nextIdx + 1;
        return e - 1;
    }

    private void setNeighbors(int idx, int prevIdx, int nextIdx) {
        link[idx] = prevIdx + 1 ^ nextIdx + 1;
    }

    // Pack two ints (prev, curr) into a single long to avoid array allocation.
    // High 32 bits = prev, low 32 bits = curr.
    private static long packPrevCurr(int prev, int curr) {
        return ((long) prev << 32) | (curr & 0xFFFFFFFFL);
    }

    private static int unpackPrev(long packed) {
        return (int) (packed >>> 32);
    }

    private static int unpackCurr(long packed) {
        return (int) packed;
    }

    // Helper that inserts idx between before and after in O(1) using XOR math.
    // before and after are adjacent neighbors in the current list (either may be -1 for ends).
    private void linkBetween(int before, int idx, int after) {
        // set neighbors for the new node first
        setNeighbors(idx, before, after);
        if (before != -1) {
            int bp = getPrev(before, after); // compute previous of 'before' quickly
            setNeighbors(before, bp, idx);
        } else {
            head = idx;
        }
        if (after != -1) {
            int an = getNext(after, before); // compute next of 'after' quickly
            setNeighbors(after, idx, an);
        } else {
            tail = idx;
        }
        size++;
        modCount++;
    }

    // Locate physical index at logical index along with its previous physical neighbor.
    // Returns a packed long where high 32 bits = prev, low 32 bits = curr. For index==size, returns pack(tail, -1).
    private long locateWithPrev(int index) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        if (index == size) {
            return packPrevCurr(tail, -1);
        }
        if (index == 0) {
            return packPrevCurr(-1, head);
        }
        if (index < (size >> 1)) {
            int prevIdx = -1;
            int i = head;
            for (int k = 0; k < index; k++) {
                int nxt = getNext(i, prevIdx);
                prevIdx = i;
                i = nxt;
            }
            return packPrevCurr(prevIdx, i);
        } else {
            int nextIdx = -1;
            int i = tail;
            for (int k = size - 1; k > index; k--) {
                int prv = getPrev(i, nextIdx);
                nextIdx = i;
                i = prv;
            }
            // i is curr at position index, nextIdx is its right neighbor
            int prevIdx = getPrev(i, nextIdx);
            return packPrevCurr(prevIdx, i);
        }
    }

    private E unlinkIndex(int idx, int p, int n) {
        @SuppressWarnings("unchecked") E old = (E) elementData[idx];
        if (p != -1) {
            int pp = getPrev(p, idx);
            setNeighbors(p, pp, n);
        } else {
            head = n;
        }
        if (n != -1) {
            int nn = getNext(n, idx);
            setNeighbors(n, p, nn);
        } else {
            tail = p;
        }
        elementData[idx] = null;
        setNeighbors(idx, -1, -1);
        freeQueue.addLast(idx);
        size--;
        modCount++;
        return old;
    }


    // Helpers to navigate logical index -> physical index
    private int physicalIndexAt(int index) {
        rangeCheck(index);
        // Choose direction for traversal
        if (index < (size >> 1)) {
            int prevIdx = -1;
            int i = head;
            for (int k = 0; k < index; k++) {
                int nxt = getNext(i, prevIdx);
                prevIdx = i;
                i = nxt;
            }
            return i;
        } else {
            int nextIdx = -1;
            int i = tail;
            for (int k = size - 1; k > index; k--) {
                int prv = getPrev(i, nextIdx);
                nextIdx = i;
                i = prv;
            }
            return i;
        }
    }

    private void rangeCheck(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    // Basic Collection/List operations
    @Override
    public int size() { return size; }

    @Override
    public boolean add(E e) { addLast(e); return true; }

    @Override
    public void addFirst(E e) {
        int idx = allocateSlot();
        elementData[idx] = e;
        // inserting before current head
        linkBetween(-1, idx, head);
    }

    @Override
    public void addLast(E e) {
        int idx = allocateSlot();
        elementData[idx] = e;
        // inserting after current tail
        linkBetween(tail, idx, -1);
    }

    @Override
    public E removeFirst() {
        if (size == 0) throw new NoSuchElementException();
        int idx = head;
        int n = getNext(idx, -1);
        return unlinkIndex(idx, -1, n);
    }

    @Override
    public E removeLast() {
        if (size == 0) throw new NoSuchElementException();
        int idx = tail;
        int p = getPrev(idx, -1);
        return unlinkIndex(idx, p, -1);
    }

    @Override
    public E getFirst() {
        if (size == 0) throw new NoSuchElementException();
        @SuppressWarnings("unchecked") E e = (E) elementData[head];
        return e;
    }

    @Override
    public E getLast() {
        if (size == 0) throw new NoSuchElementException();
        @SuppressWarnings("unchecked") E e = (E) elementData[tail];
        return e;
    }

    @Override
    public boolean offerFirst(E e) { addFirst(e); return true; }

    @Override
    public boolean offerLast(E e) { addLast(e); return true; }

    @Override
    public E pollFirst() { 
        if (size == 0) return null; 
        int idx = head; 
        int n = getNext(idx, -1);
        return unlinkIndex(idx, -1, n);
    }

    @Override
    public E pollLast() { 
        if (size == 0) return null; 
        int idx = tail; 
        int p = getPrev(idx, -1);
        return unlinkIndex(idx, p, -1);
    }

    @Override
    public E peekFirst() { return size == 0 ? null : getFirst(); }

    @Override
    public E peekLast() { return size == 0 ? null : getLast(); }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        if (size == 0) return false;
        int prev = -1;
        int i = head;
        if (o == null) {
            while (i != -1) {
                if (elementData[i] == null) { 
                    int n = getNext(i, prev);
                    unlinkIndex(i, prev, n);
                    return true; 
                }
                int nxt = getNext(i, prev);
                prev = i;
                i = nxt;
            }
        } else {
            while (i != -1) {
                if (o.equals(elementData[i])) { 
                    int n = getNext(i, prev);
                    unlinkIndex(i, prev, n);
                    return true; 
                }
                int nxt = getNext(i, prev);
                prev = i;
                i = nxt;
            }
        }
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        if (size == 0) return false;
        int nextIdx = -1;
        int i = tail;
        if (o == null) {
            while (i != -1) {
                if (elementData[i] == null) { 
                    int p = getPrev(i, nextIdx);
                    unlinkIndex(i, p, nextIdx);
                    return true; 
                }
                int prv = getPrev(i, nextIdx);
                nextIdx = i;
                i = prv;
            }
        } else {
            while (i != -1) {
                if (o.equals(elementData[i])) { 
                    int p = getPrev(i, nextIdx);
                    unlinkIndex(i, p, nextIdx);
                    return true; 
                }
                int prv = getPrev(i, nextIdx);
                nextIdx = i;
                i = prv;
            }
        }
        return false;
    }

    // Queue methods via Deque
    @Override public boolean offer(E e) { return offerLast(e); }
    @Override public E remove() { return removeFirst(); }
    @Override public E poll() { return pollFirst(); }
    @Override public E element() { return getFirst(); }
    @Override public E peek() { return peekFirst(); }
    @Override public void push(E e) { addFirst(e); }
    @Override public E pop() { return removeFirst(); }

    @Override
    public E get(int index) {
        int idx = physicalIndexAt(index);
        @SuppressWarnings("unchecked") E e = (E) elementData[idx];
        return e;
    }

    @Override
    public E set(int index, E element) {
        int idx = physicalIndexAt(index);
        @SuppressWarnings("unchecked") E old = (E) elementData[idx];
        elementData[idx] = element;
        return old;
    }

    @Override
    public void add(int index, E element) {
        rangeCheckForAdd(index);
        if (index == size) { addLast(element); return; }
        long pv = locateWithPrev(index);
        int before = unpackPrev(pv);
        int after = unpackCurr(pv);
        int idx = allocateSlot();
        elementData[idx] = element;
        linkBetween(before, idx, after);
    }

    @Override
    public E remove(int index) {
        long pv = locateWithPrev(index);
        int p = unpackPrev(pv);
        int idx = unpackCurr(pv);
        int n = getNext(idx, p);
        return unlinkIndex(idx, p, n);
    }

    @Override
    public void clear() {
        Arrays.fill(elementData, 0, allocated, null);
        Arrays.fill(link, 0, allocated, 0);
        head = tail = -1;
        size = 0;
        freeQueue.clear();
        modCount++;
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        rangeCheckForAdd(index);
        return new ABLIterator(index);
    }

    @Override
    public Iterator<E> descendingIterator() {
        ListIterator<E> it = listIterator(size());
        return new Iterator<E>() {
            @Override public boolean hasNext() { return it.hasPrevious(); }
            @Override public E next() { return it.previous(); }
            @Override public void remove() { it.remove(); }
        };
    }

    private final class ABLIterator implements ListIterator<E> {
        private int expectedModCount = modCount;
        private int nextIndex; // logical index of next()
        private int nextPhys;  // physical index of next element or -1
        private int prevOfNext; // physical index of previous element for nextPhys (or -1 if none)
        private int lastReturned = -1; // physical index of last returned by next/previous
        private boolean lastReturnedWasNext = false;

        ABLIterator(int start) {
            this.nextIndex = start;
            this.nextPhys = (start == size) ? -1 : physicalIndexAt(start);
            if (start == 0) {
                this.prevOfNext = -1;
            } else if (start == size) {
                this.prevOfNext = tail;
            } else {
                this.prevOfNext = physicalIndexAt(start - 1);
            }
        }

        private void checkForComodification() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
        }

        @Override public boolean hasNext() { return nextIndex < size; }

        @Override public E next() {
            checkForComodification();
            if (!hasNext()) throw new NoSuchElementException();
            int phys = (nextPhys == -1) ? head : nextPhys;
            @SuppressWarnings("unchecked") E e = (E) elementData[phys];
            lastReturned = phys;
            lastReturnedWasNext = true;
            // advance forward using XOR
            int prevForPhys = (nextPhys == -1) ? -1 : prevOfNext;
            int newNext = getNext(phys, prevForPhys);
            prevOfNext = phys;
            nextPhys = newNext;
            nextIndex++;
            return e;
        }

        @Override public boolean hasPrevious() { return nextIndex > 0; }

        @Override public E previous() {
            checkForComodification();
            if (!hasPrevious()) throw new NoSuchElementException();
            int phys;
            if (nextIndex == size || nextPhys == -1) {
                phys = tail;
                prevOfNext = getPrev(phys, -1);
            } else {
                phys = prevOfNext;
                // old next is nextPhys; compute new prevOfNext relative to that
                int oldNext = nextPhys;
                prevOfNext = getPrev(phys, oldNext);
            }
            @SuppressWarnings("unchecked") E e = (E) elementData[phys];
            lastReturned = phys;
            lastReturnedWasNext = false;
            nextPhys = phys; // previous() moves back, so next call to next() should return this phys's next
            nextIndex--;
            return e;
        }

        @Override public int nextIndex() { return nextIndex; }
        @Override public int previousIndex() { return nextIndex - 1; }

        @Override public void remove() {
            checkForComodification();
            if (lastReturned == -1) throw new IllegalStateException();
            int lr = lastReturned;
            int p, n;
            if (lastReturnedWasNext) {
                // After next(), cursor already advanced: nextPhys points to successor of lr
                n = nextPhys;
                p = getPrev(lr, n);
                nextIndex--;
                // nextPhys remains n
                prevOfNext = p;
            } else {
                // After previous(), cursor moved back: nextPhys equals lr, prevOfNext is predecessor of lr
                p = prevOfNext;
                n = getNext(lr, p);
                // nextIndex unchanged; nextPhys should become successor of removed lr
                nextPhys = n;
            }
            unlinkIndex(lr, p, n);
            lastReturned = -1;
            expectedModCount = modCount;
        }

        @Override public void set(E e) {
            if (lastReturned == -1) throw new IllegalStateException();
            @SuppressWarnings("unchecked") E ignore = (E) elementData[lastReturned];
            elementData[lastReturned] = e;
        }

        @Override public void add(E e) {
            checkForComodification();
            if (nextIndex == size) {
                // append to the end
                addLast(e);
                // After add, previous() must return the added element
                prevOfNext = tail; // tail is the newly added element
                nextPhys = -1; // still at end
            } else {
                int after = (nextPhys == -1) ? tail : prevOfNext;
                int idx = allocateSlot();
                elementData[idx] = e;
                // insert between 'after' and 'nextPhys'
                if (nextPhys == -1) {
                    // insert at the end after 'after'
                    linkBetween(after, idx, -1);
                } else {
                    // insert between 'after' and 'nextPhys'
                    linkBetween(after, idx, nextPhys);
                }
                // After insertion before nextPhys, next() should still return original nextPhys,
                // and previous() should return the newly inserted element
                prevOfNext = idx;
            }
            nextIndex++;
            lastReturned = -1;
            expectedModCount = modCount;
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream s) throws IOException {
        if (nonSerializableView) {
            throw new InvalidObjectException("not serializable");
        }
        s.defaultWriteObject();
        s.writeInt(size);
        for (E e : this) {
            s.writeObject(e);
        }
    }

    @Serial
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        // Reinitialize transient fields
        this.elementData = new Object[0];
        this.link = new int[0];
        this.freeQueue = new ArrayDeque<>();
        this.head = -1;
        this.tail = -1;
        this.size = 0;
        this.allocated = 0;
        int sz = s.readInt();
        for (int i = 0; i < sz; i++) {
            addLast((E) s.readObject());
        }
    }

    // Resolve default method conflict between List and Deque (both extend SequencedCollection)
    @Override
    public ArrayBackedLinkedList<E> reversed() {
        return new ReverseView<>(this);
    }

    /**
     * Reverse-order view of this list. O(1) view reflecting all changes bidirectionally.
     * Not serializable.
     */
    private static final class ReverseView<E> extends ArrayBackedLinkedList<E> {
        private final ArrayBackedLinkedList<E> forward;

        ReverseView(ArrayBackedLinkedList<E> forward) {
            super();
            this.forward = forward;
            // Mark this instance as a non-serializable view to comply with tests
            markNonSerializableView();
        }

        private int mapIndex(int index) {
            int sz = forward.size();
            if (index < 0 || index >= sz) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + sz);
            return sz - 1 - index;
        }

        private int mapIndexForAdd(int index) {
            int sz = forward.size();
            if (index < 0 || index > sz) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + sz);
            // inserting at index in reversed corresponds to inserting after mapping at forward
            // For add at end (index==sz) we insert at position 0 in forward
            return sz - index;
        }

        @Override public int size() { return forward.size(); }
        @Override public boolean isEmpty() { return forward.isEmpty(); }

        @Override public E get(int index) { return forward.get(mapIndex(index)); }
        @Override public E set(int index, E element) { return forward.set(mapIndex(index), element); }
        @Override public void add(int index, E element) { forward.add(mapIndexForAdd(index), element); }
        @Override public E remove(int index) { return forward.remove(mapIndex(index)); }

        // Deque mappings
        @Override public void addFirst(E e) { forward.addLast(e); }
        @Override public void addLast(E e) { forward.addFirst(e); }
        @Override public E removeFirst() { return forward.removeLast(); }
        @Override public E removeLast() { return forward.removeFirst(); }
        @Override public E getFirst() { return forward.getLast(); }
        @Override public E getLast() { return forward.getFirst(); }
        @Override public boolean offerFirst(E e) { return forward.offerLast(e); }
        @Override public boolean offerLast(E e) { return forward.offerFirst(e); }
        @Override public E pollFirst() { return forward.pollLast(); }
        @Override public E pollLast() { return forward.pollFirst(); }
        @Override public E peekFirst() { return forward.peekLast(); }
        @Override public E peekLast() { return forward.peekFirst(); }

        @Override public boolean removeFirstOccurrence(Object o) { return forward.removeLastOccurrence(o); }
        @Override public boolean removeLastOccurrence(Object o) { return forward.removeFirstOccurrence(o); }

        @Override public void clear() { forward.clear(); }

        @Override public ListIterator<E> listIterator(int index) {
            int sz = size();
            if (index < 0 || index > sz) throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + sz);
            ListIterator<E> it = forward.listIterator(sz - index);
            return new ListIterator<>() {
                @Override public boolean hasNext() { return it.hasPrevious(); }
                @Override public E next() { return it.previous(); }
                @Override public boolean hasPrevious() { return it.hasNext(); }
                @Override public E previous() { return it.next(); }
                @Override public int nextIndex() { return size() - it.nextIndex(); }
                @Override public int previousIndex() { return nextIndex() - 1; }
                @Override public void remove() { it.remove(); }
                @Override public void set(E e) { it.set(e); }
                @Override public void add(E e) { it.add(e); }
            };
        }

        @Override public Iterator<E> descendingIterator() {
            // Should iterate like forward iterator on original
            return forward.iterator();
        }

        @Override public ArrayBackedLinkedList<E> reversed() { return forward; }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ArrayBackedLinkedList<E> clone() {
        try {
            ArrayBackedLinkedList<E> clone = (ArrayBackedLinkedList<E>) super.clone();
            // Deep copy the transient arrays and free queue to ensure independence
            clone.elementData = Arrays.copyOf(this.elementData, this.elementData.length);
            clone.link = Arrays.copyOf(this.link, this.link.length);
            clone.freeQueue = new ArrayDeque<>(this.freeQueue);
            // Reset structural modification count for the clone (similar to ArrayList/SegmentedLinkedList behavior)
            clone.modCount = 0;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
