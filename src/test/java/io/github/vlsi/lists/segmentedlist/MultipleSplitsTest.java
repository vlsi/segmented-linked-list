package io.github.vlsi.lists.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class MultipleSplitsTest {

    @TestTemplate
    void testMultipleSplits(TestedListProvider provider) {
        List<Integer> list = provider.getList();
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }

        Spliterator<Integer> s1 = list.spliterator();
        Spliterator<Integer> s2 = s1.trySplit();
        assertNotNull(s2);

        Spliterator<Integer> s3 = s1.trySplit();
        assertNotNull(s3);

        Spliterator<Integer> s4 = s2.trySplit();
        assertNotNull(s4);

        // Collect from all splits
        List<Integer> all = new ArrayList<>();
        s1.forEachRemaining(all::add);
        s2.forEachRemaining(all::add);
        s3.forEachRemaining(all::add);
        s4.forEachRemaining(all::add);

        assertEquals(1000, all.size());
        assertEquals(1000, new HashSet<>(all).size()); // All unique
    }
}
