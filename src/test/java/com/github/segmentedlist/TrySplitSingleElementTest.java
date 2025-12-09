package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class TrySplitSingleElementTest {

    @TestTemplate
    void testTrySplitSingleElement(TestedListProvider provider) {
        List<String> list = provider.getList();
        list.add("only");

        Spliterator<String> spliterator = list.spliterator();
        assertNull(spliterator.trySplit());
    }
}
