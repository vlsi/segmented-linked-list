package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TryAdvanceTest {

    @Test
    void testTryAdvance() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c")
        );

        Spliterator<String> spliterator = list.spliterator();
        List<String> results = new ArrayList<>();

        while (spliterator.tryAdvance(results::add)) {
            // continue
        }

        assertEquals(Arrays.asList("a", "b", "c"), results);
    }
}
