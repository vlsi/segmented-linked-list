package io.github.vlsi.lists.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class TryAdvanceTest {

    @TestTemplate
    void testTryAdvance(TestedListProvider provider) {
        List<String> list = provider.getList(
                Arrays.asList("a", "b", "c")
        );

        Spliterator<String> spliterator = list.spliterator();
        List<String> results = new ArrayList<>();

        while (spliterator.tryAdvance(results::add)) {
            // continue
        }

        assertEquals(Arrays.asList("a", "b", "c"), results);
    }
}
