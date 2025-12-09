package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class StreamReduceTest {

    @TestTemplate
    void testStreamReduce(TestedListProvider provider) {
        List<Integer> list = provider.getList(
                Arrays.asList(1, 2, 3, 4, 5)
        );

        Optional<Integer> sum = list.stream()
                .reduce(Integer::sum);

        assertTrue(sum.isPresent());
        assertEquals(15, sum.get());
    }
}
