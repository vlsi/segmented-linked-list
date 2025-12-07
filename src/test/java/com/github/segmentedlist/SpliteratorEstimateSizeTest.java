package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SpliteratorEstimateSizeTest {

    @Test
    void testSpliteratorEstimateSize() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        Spliterator<Integer> spliterator = list.spliterator();
        assertEquals(100, spliterator.estimateSize());
    }
}
