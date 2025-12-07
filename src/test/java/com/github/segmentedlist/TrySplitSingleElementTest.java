package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TrySplitSingleElementTest {

    @Test
    void testTrySplitSingleElement() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>();
        list.add("only");

        Spliterator<String> spliterator = list.spliterator();
        assertNull(spliterator.trySplit());
    }
}
