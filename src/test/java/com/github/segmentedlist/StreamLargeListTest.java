package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StreamLargeListTest {

    @Test
    void testStreamLargeList() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 10000; i++) {
            list.add(i);
        }

        long count = list.stream()
                .filter(n -> n % 100 == 0)
                .count();

        assertEquals(100, count);
    }
}
