package io.github.vlsi.lists.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class ForEachConcurrentModificationTest {

    @TestTemplate
    void testForEachConcurrentModification(TestedListProvider provider) {
        List<Integer> list = provider.getList();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }

        assertThrows(ConcurrentModificationException.class, () -> {
            list.forEach(x -> list.add(999));
        });
    }
}
