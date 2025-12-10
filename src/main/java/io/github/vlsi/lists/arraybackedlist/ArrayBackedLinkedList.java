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
    transient int[] next;
    transient int[] prev;

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
        this.next = new int[0];
        this.prev = new int[0];
        this.allocated = 0;
    }

    public ArrayBackedLinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    // Capacity management (based on ArrayList grow rules)
    private void ensureCapacityInternal(int minCapacity) {
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
        next = Arrays.copyOf(next, newCap);
        prev = Arrays.copyOf(prev, newCap);
    }

    private int allocateSlot() {
        if (!freeQueue.isEmpty()) {
            return freeQueue.removeFirst();
        }
        int idx = allocated;
        if (idx >= elementData.length) {
            ensureCapacityInternal(idx + 1);
        }
        allocated = idx + 1;
        return idx;
    }

    private void linkAfter(int before, int idx) {
        // Insert after physical index 'before'. If before == -1, insert at head.
        if (before == -1) {
            // insert at head
            int oldHead = head;
            head = idx;
            prev[idx] = -1;
            next[idx] = oldHead;
            if (oldHead != -1) {
                prev[oldHead] = idx;
            } else {
                tail = idx; // first element
            }
        } else {
            int after = next[before];
            next[before] = idx;
            prev[idx] = before;
            next[idx] = after;
            if (after != -1) {
                prev[after] = idx;
            } else {
                tail = idx; // appended at end
            }
        }
        size++;
        modCount++;
    }

    private void linkBefore(int after, int idx) {
        // Insert before physical index 'after'. If after == -1, insert at tail.
        if (after == -1) {
            // insert at tail
            int oldTail = tail;
            tail = idx;
            next[idx] = -1;
            prev[idx] = oldTail;
            if (oldTail != -1) {
                next[oldTail] = idx;
            } else {
                head = idx; // first element
            }
        } else {
            int before = prev[after];
            prev[after] = idx;
            next[idx] = after;
            prev[idx] = before;
            if (before != -1) {
                next[before] = idx;
            } else {
                head = idx;
            }
        }
        size++;
        modCount++;
    }

    private E unlinkIndex(int idx) {
        @SuppressWarnings("unchecked") E old = (E) elementData[idx];
        int p = prev[idx];
        int n = next[idx];
        if (p != -1) {
            next[p] = n;
        } else {
            head = n;
        }
        if (n != -1) {
            prev[n] = p;
        } else {
            tail = p;
        }
        elementData[idx] = null;
        prev[idx] = next[idx] = -1;
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
            int i = head;
            for (int k = 0; k < index; k++) i = next[i];
            return i;
        } else {
            int i = tail;
            for (int k = size - 1; k > index; k--) i = prev[i];
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
        linkBefore(head, idx);
    }

    @Override
    public void addLast(E e) {
        int idx = allocateSlot();
        elementData[idx] = e;
        // inserting after current tail
        linkAfter(tail, idx);
    }

    @Override
    public E removeFirst() {
        if (size == 0) throw new NoSuchElementException();
        return unlinkIndex(head);
    }

    @Override
    public E removeLast() {
        if (size == 0) throw new NoSuchElementException();
        return unlinkIndex(tail);
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
    public E pollFirst() { return size == 0 ? null : unlinkIndex(head); }

    @Override
    public E pollLast() { return size == 0 ? null : unlinkIndex(tail); }

    @Override
    public E peekFirst() { return size == 0 ? null : getFirst(); }

    @Override
    public E peekLast() { return size == 0 ? null : getLast(); }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        if (size == 0) return false;
        int i = head;
        if (o == null) {
            while (i != -1) {
                if (elementData[i] == null) { unlinkIndex(i); return true; }
                i = next[i];
            }
        } else {
            while (i != -1) {
                if (o.equals(elementData[i])) { unlinkIndex(i); return true; }
                i = next[i];
            }
        }
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        if (size == 0) return false;
        int i = tail;
        if (o == null) {
            while (i != -1) {
                if (elementData[i] == null) { unlinkIndex(i); return true; }
                i = prev[i];
            }
        } else {
            while (i != -1) {
                if (o.equals(elementData[i])) { unlinkIndex(i); return true; }
                i = prev[i];
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
        int after = physicalIndexAt(index);
        int idx = allocateSlot();
        elementData[idx] = element;
        linkBefore(after, idx);
    }

    @Override
    public E remove(int index) {
        int idx = physicalIndexAt(index);
        return unlinkIndex(idx);
    }

    @Override
    public void clear() {
        Arrays.fill(elementData, 0, allocated, null);
        Arrays.fill(next, 0, allocated, 0);
        Arrays.fill(prev, 0, allocated, 0);
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
        return new Iterator<E>() {
            private int expectedModCount = modCount;
            private int curr = tail;
            private int lastReturned = -1;
            private void check() { if (expectedModCount != modCount) throw new ConcurrentModificationException(); }
            @Override public boolean hasNext() { return curr != -1; }
            @Override public E next() {
                check();
                if (curr == -1) throw new NoSuchElementException();
                int c = curr;
                curr = prev[c];
                lastReturned = c;
                @SuppressWarnings("unchecked") E e = (E) elementData[c];
                return e;
            }
            @Override public void remove() {
                check();
                if (lastReturned == -1) throw new IllegalStateException();
                int lr = lastReturned;
                // adjust curr if needed
                if (curr == lr) curr = prev[lr];
                unlinkIndex(lr);
                lastReturned = -1;
                expectedModCount = modCount;
            }
        };
    }

    private final class ABLIterator implements ListIterator<E> {
        private int expectedModCount = modCount;
        private int nextIndex; // logical index of next()
        private int nextPhys;  // physical index of next element or -1
        private int lastReturned = -1; // physical index of last returned by next/previous

        ABLIterator(int start) {
            this.nextIndex = start;
            if (start == size) {
                nextPhys = -1; // end
            } else {
                nextPhys = (start < (size >> 1)) ? head : tail;
                if (start < (size >> 1)) {
                    for (int i = 0; i < start; i++) nextPhys = next[nextPhys];
                } else {
                    // Move backwards from tail to the element at logical index 'start'
                    for (int i = size - 1; i > start; i--) nextPhys = prev[nextPhys];
                }
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
            nextPhys = next[phys];
            nextIndex++;
            return e;
        }

        @Override public boolean hasPrevious() { return nextIndex > 0; }

        @Override public E previous() {
            checkForComodification();
            if (!hasPrevious()) throw new NoSuchElementException();
            int phys;
            if (nextIndex == size) {
                phys = tail;
            } else if (nextPhys == -1) {
                phys = tail;
            } else {
                phys = prev[nextPhys];
            }
            @SuppressWarnings("unchecked") E e = (E) elementData[phys];
            lastReturned = phys;
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
            // adjust iterator's next references
            int lrNext = next[lr];
            unlinkIndex(lr);
            if (nextPhys == lr) nextPhys = lrNext; // if last op was previous()
            if (nextIndex > 0 && lrNext == nextPhys) {
                // removed element before next, shift nextIndex back
                nextIndex--;
            }
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
                addLast(e);
            } else {
                int after = (nextPhys == -1) ? tail : prev[nextPhys];
                int idx = allocateSlot();
                elementData[idx] = e;
                // insert between 'after' and 'nextPhys'
                if (nextPhys == -1) {
                    linkAfter(after, idx);
                } else {
                    linkBefore(nextPhys, idx);
                }
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
        this.next = new int[0];
        this.prev = new int[0];
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
            clone.next = Arrays.copyOf(this.next, this.next.length);
            clone.prev = Arrays.copyOf(this.prev, this.prev.length);
            clone.freeQueue = new ArrayDeque<>(this.freeQueue);
            // Reset structural modification count for the clone (similar to ArrayList/SegmentedLinkedList behavior)
            clone.modCount = 0;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
