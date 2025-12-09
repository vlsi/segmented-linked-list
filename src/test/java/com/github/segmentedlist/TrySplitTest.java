package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class TrySplitTest {

    @TestTemplate
    void testTrySplit(TestedListProvider provider) {
        List<Integer> list = provider.getList();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        Spliterator<Integer> spliterator = list.spliterator();
        Spliterator<Integer> split = spliterator.trySplit();

        assertNotNull(split);

        // Collect elements from both spliterators
        List<Integer> fromSplit = new ArrayList<>();
        split.forEachRemaining(fromSplit::add);

        List<Integer> fromOriginal = new ArrayList<>();
        spliterator.forEachRemaining(fromOriginal::add);

        // Verify split sizes
        assertTrue(fromSplit.size() > 0);
        assertTrue(fromOriginal.size() > 0);
        assertEquals(100, fromSplit.size() + fromOriginal.size());

        // Verify all elements present
        Set<Integer> allElements = new HashSet<>(fromSplit);
        allElements.addAll(fromOriginal);
        assertEquals(100, allElements.size());
    }
}
