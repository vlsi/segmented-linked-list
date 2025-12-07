package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StreamFindFirstTest {

    @Test
    void testStreamFindFirst() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
                Arrays.asList("first", "second", "third")
        );

        Optional<String> result = list.stream().findFirst();

        assertTrue(result.isPresent());
        assertEquals("first", result.get());
    }
}
