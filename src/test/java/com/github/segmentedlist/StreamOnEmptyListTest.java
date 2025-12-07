package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StreamOnEmptyListTest {

    @Test
    void testStreamOnEmptyList() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>();

        long count = list.stream().count();
        assertEquals(0, count);

        Optional<String> first = list.stream().findFirst();
        assertFalse(first.isPresent());
    }
}
