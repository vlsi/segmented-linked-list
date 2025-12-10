package io.github.vlsi.lists.segmentedlist;

import io.github.vlsi.lists.arraybackedlist.ArrayBackedLinkedList;

import java.util.*;

enum Implementation {ARRAY, LINKED, SEGMENTED, ARRAY_BACKED}

record TestedListProvider(Implementation implementation) {
    String implementationName() {
        return switch (implementation) {
            case ARRAY -> "ArrayList";
            case LINKED -> "LinkedList";
            case SEGMENTED -> "SegmentedLinkedList";
            case ARRAY_BACKED -> "ArrayBackedLinkedList";
        };
    }

    @SuppressWarnings("unchecked")
    <T> List<T> clone(List<T> list) {
        return switch (implementation) {
            case ARRAY -> (List<T>) ((ArrayList<T>) list).clone();
            case LINKED -> (List<T>) ((LinkedList<T>) list).clone();
            case SEGMENTED -> ((SegmentedLinkedList<T>) list).clone();
            case ARRAY_BACKED -> ((ArrayBackedLinkedList<T>) list).clone();
        };
    }

    <T> List<T> getList() {
        return switch (implementation) {
            case ARRAY -> new ArrayList<>();
            case LINKED -> new LinkedList<>();
            case SEGMENTED -> new SegmentedLinkedList<>();
            case ARRAY_BACKED -> new ArrayBackedLinkedList<>();
        };
    }

    <T> List<T> getList(Collection<? extends T> initial) {
        return switch (implementation) {
            case ARRAY -> new ArrayList<>(initial);
            case LINKED -> new LinkedList<>(initial);
            case SEGMENTED -> new SegmentedLinkedList<>(initial);
            case ARRAY_BACKED -> new ArrayBackedLinkedList<>(initial);
        };
    }

    <T> Deque<T> getDeque() {
        return switch (implementation) {
            case ARRAY -> throw new IllegalStateException();
            case LINKED -> new LinkedList<>();
            case SEGMENTED -> new SegmentedLinkedList<>();
            case ARRAY_BACKED -> new ArrayBackedLinkedList<>();
        };
    }

    <T> Deque<T> getDeque(Collection<? extends T> initial) {
        return switch (implementation) {
            case ARRAY -> throw new IllegalStateException();
            case LINKED -> new LinkedList<>(initial);
            case SEGMENTED -> new SegmentedLinkedList<>(initial);
            case ARRAY_BACKED -> new ArrayBackedLinkedList<>(initial);
        };
    }

    boolean isDequeSupported() {
        return implementation != Implementation.ARRAY;
    }
}
