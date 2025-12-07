package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TrySplitEmptyTest {

    @Test
    void testTrySplitEmpty() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>();
        Spliterator<String> spliterator = list.spliterator();

        assertNull(spliterator.trySplit());
    }
}
