package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class SpliteratorWithNullsTest {

    @TestTemplate
    void testSpliteratorWithNulls(TestedListProvider provider) {
        List<String> list = provider.getList(
                Arrays.asList("a", null, "b", null, "c")
        );

        List<String> result = list.stream()
                .collect(Collectors.toList());

        assertEquals(5, result.size());
        assertEquals("a", result.get(0));
        assertNull(result.get(1));
        assertEquals("b", result.get(2));
        assertNull(result.get(3));
        assertEquals("c", result.get(4));
    }
}
