package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class StreamFindFirstTest {

    @TestTemplate
    void testStreamFindFirst(TestedListProvider provider) {
        List<String> list = provider.getList(
                Arrays.asList("first", "second", "third")
        );

        Optional<String> result = list.stream().findFirst();

        assertTrue(result.isPresent());
        assertEquals("first", result.get());
    }
}
