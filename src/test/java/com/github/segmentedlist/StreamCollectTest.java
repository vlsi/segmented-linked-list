package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class StreamCollectTest {

    @Test
    void testStreamCollect() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
                Arrays.asList("apple", "banana", "cherry")
        );

        String result = list.stream()
                .collect(Collectors.joining(", "));

        assertEquals("apple, banana, cherry", result);
    }
}
