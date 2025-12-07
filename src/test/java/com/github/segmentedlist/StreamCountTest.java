package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StreamCountTest {

    @Test
    void testStreamCount() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 50; i++) {
            list.add(i);
        }

        long count = list.stream().count();
        assertEquals(50, count);
    }
}
