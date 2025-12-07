package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ForEachRemainingTest {

    @Test
    void testForEachRemaining() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c", "d", "e")
        );

        Spliterator<String> spliterator = list.spliterator();
        List<String> results = new ArrayList<>();

        // Advance once
        spliterator.tryAdvance(results::add);

        // Then use forEachRemaining
        spliterator.forEachRemaining(results::add);

        assertEquals(Arrays.asList("a", "b", "c", "d", "e"), results);
    }
}
