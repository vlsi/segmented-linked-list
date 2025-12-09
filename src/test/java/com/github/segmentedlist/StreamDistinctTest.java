package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class StreamDistinctTest {

    @TestTemplate
    void testStreamDistinct(TestedListProvider provider) {
        List<Integer> list = provider.getList(
                Arrays.asList(1, 2, 2, 3, 3, 3, 4, 4, 4, 4)
        );

        List<Integer> distinct = list.stream()
                .distinct()
                .collect(Collectors.toList());

        assertEquals(Arrays.asList(1, 2, 3, 4), distinct);
    }
}
