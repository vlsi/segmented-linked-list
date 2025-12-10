package io.github.vlsi.lists.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class StreamSortedTest {

    @TestTemplate
    void testStreamSorted(TestedListProvider provider) {
        List<Integer> list = provider.getList(
                Arrays.asList(5, 2, 8, 1, 9, 3)
        );

        List<Integer> sorted = list.stream()
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Arrays.asList(1, 2, 3, 5, 8, 9), sorted);
    }
}
