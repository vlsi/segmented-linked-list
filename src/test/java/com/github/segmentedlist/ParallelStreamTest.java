package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ParallelStreamTest {

    @Test
    void testParallelStream() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }

        long sum = list.parallelStream()
                .mapToInt(Integer::intValue)
                .sum();

        assertEquals(999 * 1000 / 2, sum);
    }
}
