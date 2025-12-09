package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class ParallelStreamTest {

    @TestTemplate
    void testParallelStream(TestedListProvider provider) {
        List<Integer> list = provider.getList();
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }

        long sum = list.parallelStream()
                .mapToInt(Integer::intValue)
                .sum();

        assertEquals(999 * 1000 / 2, sum);
    }
}
