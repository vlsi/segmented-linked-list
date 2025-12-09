package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class SpliteratorEstimateSizeTest {

    @TestTemplate
    void testSpliteratorEstimateSize(TestedListProvider provider) {
        List<Integer> list = provider.getList();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        Spliterator<Integer> spliterator = list.spliterator();
        assertEquals(100, spliterator.estimateSize());
    }
}
