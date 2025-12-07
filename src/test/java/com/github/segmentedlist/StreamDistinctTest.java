package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class StreamDistinctTest {

    @Test
    void testStreamDistinct() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>(
                Arrays.asList(1, 2, 2, 3, 3, 3, 4, 4, 4, 4)
        );

        List<Integer> distinct = list.stream()
                .distinct()
                .collect(Collectors.toList());

        assertEquals(Arrays.asList(1, 2, 3, 4), distinct);
    }
}
