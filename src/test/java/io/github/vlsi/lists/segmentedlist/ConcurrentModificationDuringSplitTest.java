package io.github.vlsi.lists.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class ConcurrentModificationDuringSplitTest {

    @TestTemplate
    void testConcurrentModificationDuringSplit(TestedListProvider provider) {
        List<Integer> list = provider.getList();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        Spliterator<Integer> spliterator = list.spliterator();

        // Modify list
        list.add(999);

        // Try to use spliterator - should throw
        assertThrows(ConcurrentModificationException.class, () -> {
            spliterator.tryAdvance(x -> {});
        });
    }
}
