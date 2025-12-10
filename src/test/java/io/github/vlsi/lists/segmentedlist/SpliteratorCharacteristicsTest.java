package io.github.vlsi.lists.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class SpliteratorCharacteristicsTest {

    @TestTemplate
    void testSpliteratorCharacteristics(TestedListProvider provider) {
        List<String> list = provider.getList(
                Arrays.asList("a", "b", "c")
        );

        Spliterator<String> spliterator = list.spliterator();

        assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED));
        assertTrue(spliterator.hasCharacteristics(Spliterator.SIZED));
        assertTrue(spliterator.hasCharacteristics(Spliterator.SUBSIZED));
        assertFalse(spliterator.hasCharacteristics(Spliterator.SORTED));
        assertFalse(spliterator.hasCharacteristics(Spliterator.DISTINCT));
        assertFalse(spliterator.hasCharacteristics(Spliterator.IMMUTABLE));
    }
}
