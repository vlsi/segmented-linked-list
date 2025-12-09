package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class StreamLargeListTest {

    @TestTemplate
    void testStreamLargeList(TestedListProvider provider) {
        List<Integer> list = provider.getList();
        for (int i = 0; i < 10000; i++) {
            list.add(i);
        }

        long count = list.stream()
                .filter(n -> n % 100 == 0)
                .count();

        assertEquals(100, count);
    }
}
