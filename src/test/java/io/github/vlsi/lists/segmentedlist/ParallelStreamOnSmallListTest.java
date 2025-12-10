package io.github.vlsi.lists.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class ParallelStreamOnSmallListTest {

    @TestTemplate
    void testParallelStreamOnSmallList(TestedListProvider provider) {
        List<Integer> list = provider.getList(
                Arrays.asList(1, 2, 3)
        );

        int sum = list.parallelStream()
                .mapToInt(Integer::intValue)
                .sum();

        assertEquals(6, sum);
    }
}
