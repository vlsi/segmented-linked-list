package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentModificationDuringSplitTest {

    @Test
    void testConcurrentModificationDuringSplit() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        Spliterator<Integer> spliterator = list.spliterator();

        // Modify list
        list.add(999);

        // Try to use spliterator - should throw
        assertThrows(ConcurrentModificationException.class, () -> {
            spliterator.tryAdvance(x -> {});
        });
    }
}
