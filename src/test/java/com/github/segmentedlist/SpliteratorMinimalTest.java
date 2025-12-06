package com.github.segmentedlist;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.*;

class SpliteratorMinimalTest {

    @Test
    void testSpliteratorCharacteristics() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c")
        );

        Spliterator<String> spliterator = list.spliterator();

        assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED));
        assertTrue(spliterator.hasCharacteristics(Spliterator.SIZED));
        assertTrue(spliterator.hasCharacteristics(Spliterator.SUBSIZED));
    }
}
