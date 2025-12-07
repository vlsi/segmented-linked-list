package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentModificationDuringTraversalTest {

    @Test
    void testConcurrentModificationDuringTraversal() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        Spliterator<Integer> spliterator = list.spliterator();

        // Advance a few times
        spliterator.tryAdvance(x -> {});
        spliterator.tryAdvance(x -> {});

        // Modify list
        list.add(999);

        // Try to continue - should throw
        assertThrows(ConcurrentModificationException.class, () -> {
            spliterator.tryAdvance(x -> {});
        });
    }
}
