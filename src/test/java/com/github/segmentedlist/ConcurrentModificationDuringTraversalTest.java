package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class ConcurrentModificationDuringTraversalTest {

    @TestTemplate
    void testConcurrentModificationDuringTraversal(TestedListProvider provider) {
        List<Integer> list = provider.getList();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        Spliterator<Integer> spliterator = list.spliterator();

        // Advance a few times
        spliterator.tryAdvance(x -> {});
        spliterator.tryAdvance(x -> {});

        // Modify list
        list.add(999);

        // Try to continue - should throw
        assertThrows(ConcurrentModificationException.class, () -> {
            spliterator.tryAdvance(x -> {});
        });
    }
}
