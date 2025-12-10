package io.github.vlsi.lists.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ComparisonTestExtension.class)
class StreamCollectTest {

    @TestTemplate
    void testStreamCollect(TestedListProvider provider) {
        List<String> list = provider.getList(
                Arrays.asList("apple", "banana", "cherry")
        );

        String result = list.stream()
                .collect(Collectors.joining(", "));

        assertEquals("apple, banana, cherry", result);
    }
}
