package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ForEachOptimizationTest {

    @Test
    void testForEachOptimization() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        AtomicInteger sum = new AtomicInteger(0);
        list.forEach(sum::addAndGet);

        assertEquals(4950, sum.get());
    }
}
