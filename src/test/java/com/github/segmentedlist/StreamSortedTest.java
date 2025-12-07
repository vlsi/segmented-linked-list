package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class StreamSortedTest {

    @Test
    void testStreamSorted() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>(
                Arrays.asList(5, 2, 8, 1, 9, 3)
        );

        List<Integer> sorted = list.stream()
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Arrays.asList(1, 2, 3, 5, 8, 9), sorted);
    }
}
