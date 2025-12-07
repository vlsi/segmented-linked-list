package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ParallelStreamOnSmallListTest {

    @Test
    void testParallelStreamOnSmallList() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>(
                Arrays.asList(1, 2, 3)
        );

        int sum = list.parallelStream()
                .mapToInt(Integer::intValue)
                .sum();

        assertEquals(6, sum);
    }
}
