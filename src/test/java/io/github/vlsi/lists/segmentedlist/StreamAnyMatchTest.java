package io.github.vlsi.lists.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class StreamAnyMatchTest {

    @TestTemplate
    void testStreamAnyMatch(TestedListProvider provider) {
        List<Integer> list = provider.getList(
                Arrays.asList(1, 2, 3, 4, 5)
        );

        assertTrue(list.stream().anyMatch(n -> n > 3));
        assertFalse(list.stream().anyMatch(n -> n > 10));
    }
}
