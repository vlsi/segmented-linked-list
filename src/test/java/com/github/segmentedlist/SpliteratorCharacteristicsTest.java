package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SpliteratorCharacteristicsTest {

    @Test
    void testSpliteratorCharacteristics() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c")
        );

        Spliterator<String> spliterator = list.spliterator();

        assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED));
        assertTrue(spliterator.hasCharacteristics(Spliterator.SIZED));
        assertTrue(spliterator.hasCharacteristics(Spliterator.SUBSIZED));
        assertFalse(spliterator.hasCharacteristics(Spliterator.SORTED));
        assertFalse(spliterator.hasCharacteristics(Spliterator.DISTINCT));
        assertFalse(spliterator.hasCharacteristics(Spliterator.IMMUTABLE));
    }
}
