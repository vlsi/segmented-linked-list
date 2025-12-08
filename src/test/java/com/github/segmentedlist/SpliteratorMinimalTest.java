package com.github.segmentedlist;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ComparisonTestExtension.class)
class SpliteratorMinimalTest {


    @TestTemplate
    void testSpliteratorCharacteristics(TestedListProvider provider) {
        Spliterator<String> spliterator = provider.<String>getList().spliterator();
        assertTrue(spliterator.hasCharacteristics(Spliterator.ORDERED));
        assertTrue(spliterator.hasCharacteristics(Spliterator.SIZED));
        assertTrue(spliterator.hasCharacteristics(Spliterator.SUBSIZED));
    }
}
