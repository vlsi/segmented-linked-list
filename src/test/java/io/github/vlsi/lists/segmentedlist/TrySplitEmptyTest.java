package io.github.vlsi.lists.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class TrySplitEmptyTest {

    @TestTemplate
    void testTrySplitEmpty(TestedListProvider provider) {
        List<String> list = provider.getList();
        Spliterator<String> spliterator = list.spliterator();

        assertNull(spliterator.trySplit());
    }
}
