package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class StreamAnyMatchTest {

    @Test
    void testStreamAnyMatch() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>(
                Arrays.asList(1, 2, 3, 4, 5)
        );

        assertTrue(list.stream().anyMatch(n -> n > 3));
        assertFalse(list.stream().anyMatch(n -> n > 10));
    }
}
