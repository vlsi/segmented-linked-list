package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SpliteratorWithNullsTest {

    @Test
    void testSpliteratorWithNulls() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
                Arrays.asList("a", null, "b", null, "c")
        );

        List<String> result = list.stream()
                .collect(Collectors.toList());

        assertEquals(5, result.size());
        assertEquals("a", result.get(0));
        assertNull(result.get(1));
        assertEquals("b", result.get(2));
        assertNull(result.get(3));
        assertEquals("c", result.get(4));
    }
}
