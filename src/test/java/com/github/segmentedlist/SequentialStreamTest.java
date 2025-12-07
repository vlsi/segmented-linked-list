package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SequentialStreamTest {

    @Test
    void testSequentialStream() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        List<Integer> result = list.stream()
                .filter(n -> n % 2 == 0)
                .map(n -> n * 2)
                .collect(Collectors.toList());

        assertEquals(50, result.size());
        assertEquals(0, result.get(0));
        assertEquals(196, result.get(49));
    }
}
