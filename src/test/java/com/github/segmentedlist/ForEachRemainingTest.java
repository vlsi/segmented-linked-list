package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class ForEachRemainingTest {

    @TestTemplate
    void testForEachRemaining(TestedListProvider provider) {
        List<String> list = provider.getList(
                Arrays.asList("a", "b", "c", "d", "e")
        );

        Spliterator<String> spliterator = list.spliterator();
        List<String> results = new ArrayList<>();

        // Advance once
        spliterator.tryAdvance(results::add);

        // Then use forEachRemaining
        spliterator.forEachRemaining(results::add);

        assertEquals(Arrays.asList("a", "b", "c", "d", "e"), results);
    }
}
