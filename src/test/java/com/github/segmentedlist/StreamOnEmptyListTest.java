package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class StreamOnEmptyListTest {

    @TestTemplate
    void testStreamOnEmptyList(TestedListProvider provider) {
        List<String> list = provider.getList();

        long count = list.stream().count();
        assertEquals(0, count);

        Optional<String> first = list.stream().findFirst();
        assertFalse(first.isPresent());
    }
}
