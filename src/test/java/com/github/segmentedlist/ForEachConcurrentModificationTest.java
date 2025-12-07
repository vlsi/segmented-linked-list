package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ForEachConcurrentModificationTest {

    @Test
    void testForEachConcurrentModification() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }

        assertThrows(ConcurrentModificationException.class, () -> {
            list.forEach(x -> list.add(999));
        });
    }
}
