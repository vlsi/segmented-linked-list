package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StreamReduceTest {

    @Test
    void testStreamReduce() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>(
                Arrays.asList(1, 2, 3, 4, 5)
        );

        Optional<Integer> sum = list.stream()
                .reduce(Integer::sum);

        assertTrue(sum.isPresent());
        assertEquals(15, sum.get());
    }
}
