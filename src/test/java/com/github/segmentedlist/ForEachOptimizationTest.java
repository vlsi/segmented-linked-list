package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class ForEachOptimizationTest {

    @TestTemplate
    void testForEachOptimization(TestedListProvider provider) {
        List<Integer> list = provider.getList();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        AtomicInteger sum = new AtomicInteger(0);
        list.forEach(sum::addAndGet);

        assertEquals(4950, sum.get());
    }
}
