package com.github.segmentedlist;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * A {@link List} implementation that combines the benefits of linked lists and arrays
 * using a segmented approach. Each node contains an array of elements (segment),
 * reducing memory overhead and improving cache locality compared to traditional linked lists.
 *
 * <p>This implementation provides constant-time insertions and removals at both ends,
 * and linear-time access to elements by index. The segment size is fixed at 16 elements,
 * providing a good balance between memory efficiency and performance.</p>
 *
 * <p><strong>Time Complexity:</strong></p>
 * <ul>
 *   <li>add(E): O(1) amortized</li>
 *   <li>add(int, E): O(n)</li>
 *   <li>get(int): O(n/16)</li>
 *   <li>remove(int): O(n)</li>
 *   <li>size(): O(1)</li>
 * </ul>
 *
 * <p>This implementation is not synchronized. If multiple threads access a list
 * concurrently, and at least one of the threads modifies the list structurally,
 * it must be synchronized externally.</p>
 *
 * <p>The iterators returned by this class's {@code iterator} and {@code listIterator}
 * methods are <i>fail-fast</i>: if the list is structurally modified at any time after
 * the iterator is created, the iterator will throw a {@link ConcurrentModificationException}.</p>
 *
 * @param <E> the type of elements held in this list
 * @author SegmentedList Contributors
 * @since 1.0
 */
public class SegmentedLinkedList<E> extends AbstractSequentialList<E>
        implements List<E>, Deque<E>, Cloneable, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The number of elements each segment can hold.
     */
    private static final int SEGMENT_SIZE = 16;

    /**
     * The first segment in the list.
     */
    private transient Segment<E> first;

    /**
     * The last segment in the list.
     */
    private transient Segment<E> last;

    /**
     * The total number of elements in the list.
     */
    private transient int size;

    /**
     * The number of times this list has been structurally modified.
     */
    private transient int modCount;

    /**
     * Constructs an empty list.
     */
    public SegmentedLinkedList() {
    }

    /**
     * Constructs a list containing the elements of the specified collection,
     * in the order they are returned by the collection's iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public SegmentedLinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    /**
     * A segment node containing an array of elements.
     *
     * @param <E> the type of elements stored in this segment
     */
    private static final class Segment<E> {
        Object[] elements;
        int size;
        Segment<E> prev;
        Segment<E> next;

        Segment() {
            this.elements = new Object[SEGMENT_SIZE];
            this.size = 0;
        }

        boolean isFull() {
            return size == SEGMENT_SIZE;
        }

        boolean isEmpty() {
            return size == 0;
        }

        @SuppressWarnings("unchecked")
        E get(int index) {
            return (E) elements[index];
        }

        void set(int index, E element) {
            elements[index] = element;
        }

        void add(E element) {
            elements[size++] = element;
        }

        void add(int index, E element) {
            System.arraycopy(elements, index, elements, index + 1, size - index);
            elements[index] = element;
            size++;
        }

        E remove(int index) {
            @SuppressWarnings("unchecked")
            E oldValue = (E) elements[index];
            int numMoved = size - index - 1;
            if (numMoved > 0) {
                System.arraycopy(elements, index + 1, elements, index, numMoved);
            }
            elements[--size] = null;
            return oldValue;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean add(E e) {
        addLast(e);
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        if (c.isEmpty()) {
            return false;
        }

        // For adding at the end, optimize by filling segments efficiently
        if (index == size) {
            for (E element : c) {
                addLast(element);
            }
            return true;
        }

        // For other positions, use the inherited implementation via listIterator
        // This is still better than the default AbstractSequentialList implementation
        // because we provide an efficient listIterator
        return super.addAll(index, c);
    }

    @Override
    public void addFirst(E e) {
        if (first == null) {
            first = last = new Segment<>();
            first.add(e);
        } else if (first.size < SEGMENT_SIZE) {
            first.add(0, e);
        } else {
            Segment<E> newSegment = new Segment<>();
            newSegment.add(e);
            newSegment.next = first;
            first.prev = newSegment;
            first = newSegment;
        }
        size++;
        modCount++;
    }

    @Override
    public void addLast(E e) {
        if (last == null) {
            first = last = new Segment<>();
            last.add(e);
        } else if (!last.isFull()) {
            last.add(e);
        } else {
            Segment<E> newSegment = new Segment<>();
            newSegment.add(e);
            newSegment.prev = last;
            last.next = newSegment;
            last = newSegment;
        }
        size++;
        modCount++;
    }

    @Override
    public E removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        E element = first.get(0);
        first.remove(0);
        size--;
        modCount++;
        if (first.isEmpty()) {
            first = first.next;
            if (first == null) {
                last = null;
            } else {
                first.prev = null;
            }
        }
        return element;
    }

    @Override
    public E removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        E element = last.get(last.size - 1);
        last.remove(last.size - 1);
        size--;
        modCount++;
        if (last.isEmpty()) {
            last = last.prev;
            if (last == null) {
                first = null;
            } else {
                last.next = null;
            }
        }
        return element;
    }

    @Override
    public E getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return first.get(0);
    }

    @Override
    public E getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return last.get(last.size - 1);
    }

    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        addLast(e);
        return true;
    }

    @Override
    public E pollFirst() {
        return isEmpty() ? null : removeFirst();
    }

    @Override
    public E pollLast() {
        return isEmpty() ? null : removeLast();
    }

    @Override
    public E peekFirst() {
        return isEmpty() ? null : getFirst();
    }

    @Override
    public E peekLast() {
        return isEmpty() ? null : getLast();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        for (ListIterator<E> it = listIterator(); it.hasNext(); ) {
            if (Objects.equals(o, it.next())) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        for (ListIterator<E> it = listIterator(size); it.hasPrevious(); ) {
            if (Objects.equals(o, it.previous())) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean offer(E e) {
        return offerLast(e);
    }

    @Override
    public E remove() {
        return removeFirst();
    }

    @Override
    public E poll() {
        return pollFirst();
    }

    @Override
    public E element() {
        return getFirst();
    }

    @Override
    public E peek() {
        return peekFirst();
    }

    @Override
    public void push(E e) {
        addFirst(e);
    }

    @Override
    public E pop() {
        return removeFirst();
    }

    @Override
    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        Segment<E> segment;
        int segmentIndex;

        if (index < size / 2) {
            // Navigate from the beginning (closer to start)
            int remaining = index;
            segment = first;
            while (segment != null && remaining >= segment.size) {
                remaining -= segment.size;
                segment = segment.next;
            }
            segmentIndex = remaining;
        } else {
            // Navigate from the end (closer to end)
            int remaining = size - index - 1;
            segment = last;

            // Navigate backward to find the segment
            while (segment != null && remaining >= segment.size) {
                remaining -= segment.size;
                segment = segment.prev;
            }

            // Position is at (segment.size - remaining - 1)
            segmentIndex = segment.size - remaining - 1;
        }

        return segment.get(segmentIndex);
    }

    @Override
    public E set(int index, E element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        Segment<E> segment;
        int segmentIndex;

        if (index < size / 2) {
            // Navigate from the beginning (closer to start)
            int remaining = index;
            segment = first;
            while (segment != null && remaining >= segment.size) {
                remaining -= segment.size;
                segment = segment.next;
            }
            segmentIndex = remaining;
        } else {
            // Navigate from the end (closer to end)
            int remaining = size - index - 1;
            segment = last;

            // Navigate backward to find the segment
            while (segment != null && remaining >= segment.size) {
                remaining -= segment.size;
                segment = segment.prev;
            }

            // Position is at (segment.size - remaining - 1)
            segmentIndex = segment.size - remaining - 1;
        }

        E oldValue = segment.get(segmentIndex);
        segment.set(segmentIndex, element);
        return oldValue;
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return new SegmentedListIterator(index);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new Iterator<>() {
            private final ListIterator<E> it = listIterator(size);

            @Override
            public boolean hasNext() {
                return it.hasPrevious();
            }

            @Override
            public E next() {
                return it.previous();
            }

            @Override
            public void remove() {
                it.remove();
            }
        };
    }

    @Override
    public SegmentedLinkedList<E> reversed() {
        return new ReverseOrderSegmentedLinkedListView<>(this);
    }

    /**
     * A reverse-order view of a SegmentedLinkedList.
     * This is an O(1) view that delegates all operations to the underlying list in reverse.
     * Modifications to the view affect the original list and vice versa.
     *
     * <p>Note: This view cannot be serialized. Attempts to serialize will result in
     * an {@link java.io.InvalidObjectException}. To serialize a reversed list, create
     * a new list from the reversed view instead.</p>
     */
    private static class ReverseOrderSegmentedLinkedListView<E> extends SegmentedLinkedList<E>
            implements java.io.Externalizable {
        private final SegmentedLinkedList<E> forward;

        ReverseOrderSegmentedLinkedListView(SegmentedLinkedList<E> forward) {
            super(); // Don't initialize the parent's fields
            this.forward = forward;
        }

        @Override
        public int size() {
            return forward.size();
        }

        @Override
        public boolean isEmpty() {
            return forward.isEmpty();
        }

        @Override
        public E get(int index) {
            return forward.get(size() - 1 - index);
        }

        @Override
        public E set(int index, E element) {
            return forward.set(size() - 1 - index, element);
        }

        @Override
        public void add(int index, E element) {
            forward.add(size() - index, element);
        }

        @Override
        public E remove(int index) {
            return forward.remove(size() - 1 - index);
        }

        @Override
        public void addFirst(E e) {
            forward.addLast(e);
        }

        @Override
        public void addLast(E e) {
            forward.addFirst(e);
        }

        @Override
        public E removeFirst() {
            return forward.removeLast();
        }

        @Override
        public E removeLast() {
            return forward.removeFirst();
        }

        @Override
        public E getFirst() {
            return forward.getLast();
        }

        @Override
        public E getLast() {
            return forward.getFirst();
        }

        @Override
        public ListIterator<E> listIterator(int index) {
            return new ListIterator<>() {
                private final ListIterator<E> forwardIterator = forward.listIterator(size() - index);

                @Override
                public boolean hasNext() {
                    return forwardIterator.hasPrevious();
                }

                @Override
                public E next() {
                    return forwardIterator.previous();
                }

                @Override
                public boolean hasPrevious() {
                    return forwardIterator.hasNext();
                }

                @Override
                public E previous() {
                    return forwardIterator.next();
                }

                @Override
                public int nextIndex() {
                    return size() - forwardIterator.nextIndex();
                }

                @Override
                public int previousIndex() {
                    return nextIndex() - 1;
                }

                @Override
                public void remove() {
                    forwardIterator.remove();
                }

                @Override
                public void set(E e) {
                    forwardIterator.set(e);
                }

                @Override
                public void add(E e) {
                    forwardIterator.add(e);
                    forwardIterator.previous();
                }
            };
        }

        @Override
        public Iterator<E> descendingIterator() {
            return forward.iterator();
        }

        @Override
        public SegmentedLinkedList<E> reversed() {
            return forward;
        }

        @Override
        public void clear() {
            forward.clear();
        }

        @Override
        public boolean removeFirstOccurrence(Object o) {
            return forward.removeLastOccurrence(o);
        }

        @Override
        public boolean removeLastOccurrence(Object o) {
            return forward.removeFirstOccurrence(o);
        }

        @Override
        public Spliterator<E> spliterator() {
            // Create reversed spliterator starting from the end
            return new SegmentedSpliterator<>(forward, forward.last,
                    forward.last == null ? 0 : forward.last.size - 1,
                    forward.size, forward.modCount, true);
        }

        @Override
        public void forEach(Consumer<? super E> action) {
            // Iterate in reverse order
            Objects.requireNonNull(action);
            int mc = forward.modCount;

            // Iterate segments backward
            for (Segment<E> seg = forward.last; seg != null; seg = seg.prev) {
                // Within each segment, iterate backward
                for (int i = seg.size - 1; i >= 0; i--) {
                    action.accept(seg.get(i));
                    // Check for concurrent modification after each element
                    if (forward.modCount != mc) {
                        throw new ConcurrentModificationException();
                    }
                }
            }

            if (forward.modCount != mc) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public SegmentedLinkedList<E> clone() {
            // Return a reversed view of the cloned forward list
            return new ReverseOrderSegmentedLinkedListView<>(forward.clone());
        }

        @Override
        public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
            throw new java.io.InvalidObjectException("not serializable");
        }

        @Override
        public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException {
            throw new java.io.InvalidObjectException("not serializable");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public SegmentedLinkedList<E> clone() {
        try {
            SegmentedLinkedList<E> clone = (SegmentedLinkedList<E>) super.clone();
            clone.first = null;
            clone.last = null;
            clone.size = 0;
            clone.modCount = 0;

            // Copy segments directly for better performance
            for (Segment<E> seg = first; seg != null; seg = seg.next) {
                Segment<E> newSegment = new Segment<>();
                System.arraycopy(seg.elements, 0, newSegment.elements, 0, seg.size);
                newSegment.size = seg.size;

                if (clone.first == null) {
                    clone.first = clone.last = newSegment;
                } else {
                    clone.last.next = newSegment;
                    newSegment.prev = clone.last;
                    clone.last = newSegment;
                }
                clone.size += newSegment.size;
            }

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Serial
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(size);
        for (E element : this) {
            s.writeObject(element);
        }
    }

    @Serial
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int size = s.readInt();
        for (int i = 0; i < size; i++) {
            add((E) s.readObject());
        }
    }

    @Override
    public void clear() {
        for (Segment<E> seg = first; seg != null; ) {
            Segment<E> next = seg.next;
            seg.elements = null;
            seg.prev = null;
            seg.next = null;
            seg = next;
        }
        first = last = null;
        size = 0;
        modCount++;
    }

    @Override
    public Spliterator<E> spliterator() {
        return new SegmentedSpliterator<>(this, first, 0, size, modCount);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        int mc = modCount;

        for (Segment<E> seg = first; seg != null; seg = seg.next) {
            for (int i = 0; i < seg.size; i++) {
                action.accept(seg.get(i));
                // Check for concurrent modification after each element
                if (modCount != mc) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        if (modCount != mc) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Custom Spliterator implementation optimized for segmented structure.
     * Provides efficient splitting at segment boundaries and direct element access.
     */
    private static final class SegmentedSpliterator<E> implements Spliterator<E> {
        private final SegmentedLinkedList<E> list;
        private Segment<E> current;
        private int segmentIndex;
        private int remaining;
        private final int expectedModCount;
        private final boolean reversed;

        SegmentedSpliterator(SegmentedLinkedList<E> list, Segment<E> segment,
                            int segmentIndex, int remaining, int expectedModCount) {
            this(list, segment, segmentIndex, remaining, expectedModCount, false);
        }

        SegmentedSpliterator(SegmentedLinkedList<E> list, Segment<E> segment,
                            int segmentIndex, int remaining, int expectedModCount, boolean reversed) {
            this.list = list;
            this.current = segment;
            this.segmentIndex = segmentIndex;
            this.remaining = remaining;
            this.expectedModCount = expectedModCount;
            this.reversed = reversed;
        }

        @Override
        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }

            if (remaining <= 0 || current == null) {
                return false;
            }

            checkForComodification();

            if (reversed) {
                // Reversed iteration: move backward
                action.accept(current.get(segmentIndex--));
                remaining--;

                // Move to previous segment if we've exhausted current one
                if (segmentIndex < 0) {
                    current = current.prev;
                    segmentIndex = current == null ? 0 : current.size - 1;
                }
            } else {
                // Forward iteration
                action.accept(current.get(segmentIndex++));
                remaining--;

                // Move to next segment if we've exhausted current one
                if (segmentIndex >= current.size) {
                    current = current.next;
                    segmentIndex = 0;
                }
            }

            return true;
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }

            int r = remaining;
            if (r <= 0) {
                return;
            }

            checkForComodification();

            Segment<E> seg = current;
            int idx = segmentIndex;

            if (reversed) {
                // Reversed iteration: process backward
                // Process remaining elements in current segment (backward)
                if (seg != null && idx >= 0) {
                    int toProcess = Math.min(idx + 1, r);
                    for (int i = idx; i >= idx - toProcess + 1; i--) {
                        action.accept(seg.get(i));
                    }
                    r -= toProcess;
                    seg = seg.prev;
                }

                // Process complete segments (backward)
                while (r > 0 && seg != null) {
                    int toProcess = Math.min(r, seg.size);
                    if (toProcess > 0) {
                        for (int i = seg.size - 1; i >= seg.size - toProcess; i--) {
                            action.accept(seg.get(i));
                        }
                        r -= toProcess;
                    }
                    // Always move to next segment (even if current was empty)
                    seg = seg.prev;
                }
            } else {
                // Forward iteration
                // Process remaining elements in current segment
                if (seg != null && idx < seg.size) {
                    int end = Math.min(idx + r, seg.size);
                    for (int i = idx; i < end; i++) {
                        action.accept(seg.get(i));
                    }
                    r -= (end - idx);
                    seg = seg.next;
                }

                // Process complete segments
                while (r > 0 && seg != null) {
                    int toProcess = Math.min(r, seg.size);
                    if (toProcess > 0) {
                        for (int i = 0; i < toProcess; i++) {
                            action.accept(seg.get(i));
                        }
                        r -= toProcess;
                    }
                    // Always move to next segment (even if current was empty)
                    seg = seg.next;
                }
            }

            remaining = 0;
            current = null;
            segmentIndex = 0;

            checkForComodification();
        }

        @Override
        public Spliterator<E> trySplit() {
            if (remaining <= 1 || current == null) {
                return null;
            }

            checkForComodification();

            // Calculate split size (split roughly in half)
            int splitSize = remaining / 2;
            if (splitSize == 0) {
                return null;
            }

            // Navigate to the split point
            int toSkip = splitSize;
            Segment<E> splitSegment = current;
            int splitIndex = segmentIndex;

            if (reversed) {
                // For reversed, navigate backward
                while (toSkip > 0 && splitSegment != null) {
                    int available = splitIndex + 1;
                    if (available <= 0) {
                        // This can happen if splitIndex < 0
                        // This shouldn't normally happen in a well-formed spliterator state
                        // Return null to indicate we can't split further
                        return null;
                    }
                    if (toSkip < available) {
                        splitIndex -= toSkip;
                        toSkip = 0;  // Set toSkip to 0 to mark that we successfully found the split point
                        break;
                    }
                    toSkip -= available;
                    splitSegment = splitSegment.prev;
                    splitIndex = splitSegment == null ? 0 : splitSegment.size - 1;
                }
            } else {
                // For forward, navigate forward
                while (toSkip > 0 && splitSegment != null) {
                    int available = splitSegment.size - splitIndex;
                    if (available <= 0) {
                        // This can happen if splitIndex >= segment.size or segment is empty
                        // This shouldn't normally happen in a well-formed spliterator state
                        // Return null to indicate we can't split further
                        return null;
                    }
                    if (toSkip < available) {
                        splitIndex += toSkip;
                        toSkip = 0;  // Set toSkip to 0 to mark that we successfully found the split point
                        break;
                    }
                    toSkip -= available;
                    splitSegment = splitSegment.next;
                    splitIndex = 0;
                }
            }

            if (splitSegment == null || toSkip > 0) {
                // Couldn't navigate to split point - can't split
                return null;
            }

            // Create spliterator for the prefix (first half)
            Spliterator<E> prefix = new SegmentedSpliterator<>(
                    list, current, segmentIndex, splitSize, expectedModCount, reversed);

            // Update this spliterator to cover the suffix (second half)
            current = splitSegment;
            segmentIndex = splitIndex;
            remaining -= splitSize;

            return prefix;
        }

        @Override
        public long estimateSize() {
            return remaining;
        }

        @Override
        public int characteristics() {
            return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        private void checkForComodification() {
            if (list.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * ListIterator implementation for SegmentedLinkedList.
     */
    private final class SegmentedListIterator implements ListIterator<E> {
        private Segment<E> currentSegment;
        private int segmentIndex;
        private int nextIndex;
        private int expectedModCount;
        private Segment<E> lastReturnedSegment;
        private int lastReturnedSegmentIndex;
        private boolean canRemove;
        private int lastDirection; // 1 for next(), -1 for previous(), 0 for none

        SegmentedListIterator(int index) {
            this.expectedModCount = modCount;
            this.nextIndex = index;
            this.canRemove = false;

            if (index == size) {
                currentSegment = null;
                segmentIndex = 0;
            } else if (index == 0) {
                currentSegment = first;
                segmentIndex = 0;
            } else if (index < size / 2) {
                // Navigate from the beginning (closer to start)
                int remaining = index;
                currentSegment = first;
                while (currentSegment != null && remaining >= currentSegment.size) {
                    remaining -= currentSegment.size;
                    currentSegment = currentSegment.next;
                }
                segmentIndex = remaining;
            } else {
                // Navigate from the end (closer to end)
                int remaining = size - index;
                currentSegment = last;

                // Navigate backward to find the segment
                while (currentSegment != null && remaining > currentSegment.size) {
                    remaining -= currentSegment.size;
                    currentSegment = currentSegment.prev;
                }

                // Position is at (segment.size - remaining)
                segmentIndex = currentSegment == null ? 0 : currentSegment.size - remaining;
            }
        }

        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }

        @Override
        public E next() {
            checkForComodification();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            if (currentSegment == null) {
                currentSegment = first;
                segmentIndex = 0;
            }

            E element = currentSegment.get(segmentIndex);
            lastReturnedSegment = currentSegment;
            lastReturnedSegmentIndex = segmentIndex;
            canRemove = true;
            lastDirection = 1; // Moving forward

            segmentIndex++;
            if (segmentIndex >= currentSegment.size) {
                currentSegment = currentSegment.next;
                segmentIndex = 0;
            }

            nextIndex++;
            return element;
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        @Override
        public E previous() {
            checkForComodification();
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }

            if (currentSegment == null) {
                currentSegment = last;
                segmentIndex = currentSegment.size;
            }

            if (segmentIndex == 0) {
                currentSegment = currentSegment.prev;
                segmentIndex = currentSegment.size;
            }

            segmentIndex--;
            E element = currentSegment.get(segmentIndex);
            lastReturnedSegment = currentSegment;
            lastReturnedSegmentIndex = segmentIndex;
            canRemove = true;
            lastDirection = -1; // Moving backward

            nextIndex--;
            return element;
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        @Override
        public void remove() {
            checkForComodification();
            if (!canRemove) {
                throw new IllegalStateException();
            }

            Segment<E> segment = lastReturnedSegment;
            int index = lastReturnedSegmentIndex;

            segment.remove(index);
            SegmentedLinkedList.this.size--;
            SegmentedLinkedList.this.modCount++;
            expectedModCount = SegmentedLinkedList.this.modCount;

            if (segment.isEmpty()) {
                Segment<E> prevSeg = segment.prev;
                Segment<E> nextSeg = segment.next;

                if (prevSeg == null) {
                    SegmentedLinkedList.this.first = nextSeg;
                } else {
                    prevSeg.next = nextSeg;
                }

                if (nextSeg == null) {
                    SegmentedLinkedList.this.last = prevSeg;
                } else {
                    nextSeg.prev = prevSeg;
                }

                if (segment == currentSegment) {
                    currentSegment = nextSeg;
                    segmentIndex = 0;
                }
            } else {
                if (segment == currentSegment && index < segmentIndex) {
                    segmentIndex--;
                }
            }

            // If last operation was next(), decrement nextIndex
            // If last operation was previous(), nextIndex stays the same
            if (lastDirection == 1) {
                nextIndex--;
            }

            canRemove = false;
            lastDirection = 0;
        }

        @Override
        public void set(E e) {
            checkForComodification();
            if (!canRemove) {
                throw new IllegalStateException();
            }
            lastReturnedSegment.set(lastReturnedSegmentIndex, e);
        }

        @Override
        public void add(E e) {
            checkForComodification();
            canRemove = false;

            if (currentSegment == null) {
                if (size == 0) {
                    Segment<E> newSeg = new Segment<>();
                    newSeg.add(e);
                    first = last = newSeg;
                    currentSegment = first;
                    segmentIndex = 1;
                    size++;
                    modCount++;
                } else {
                    addLast(e);
                    currentSegment = null;
                    segmentIndex = 0;
                }
            } else if (!currentSegment.isFull()) {
                currentSegment.add(segmentIndex, e);
                segmentIndex++;
                size++;
                modCount++;
            } else {
                // Need to split or insert new segment
                Segment<E> newSegment = new Segment<>();
                newSegment.add(e);

                Segment<E> prevSeg = currentSegment.prev;
                newSegment.prev = prevSeg;
                newSegment.next = currentSegment;

                if (prevSeg == null) {
                    first = newSegment;
                } else {
                    prevSeg.next = newSegment;
                }
                currentSegment.prev = newSegment;

                currentSegment = newSegment;
                segmentIndex = 1;
                size++;
                modCount++;
            }

            nextIndex++;
            expectedModCount = modCount;
        }

        private void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
