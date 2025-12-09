package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class StreamCountTest {

    @TestTemplate
    void testStreamCount(TestedListProvider provider) {
        List<Integer> list = provider.getList();
        for (int i = 0; i < 50; i++) {
            list.add(i);
        }

        long count = list.stream().count();
        assertEquals(50, count);
    }
}
