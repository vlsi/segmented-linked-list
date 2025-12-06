package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Spliterator and stream operations on {@link SegmentedLinkedList}.
 */
class SpliteratorTest {

    @Test
    void testSpliteratorCharacteristics() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
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

    @Test
    void testSpliteratorEstimateSize() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        Spliterator<Integer> spliterator = list.spliterator();
        assertEquals(100, spliterator.estimateSize());
    }

    @Test
    void testTryAdvance() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c")
        );

        Spliterator<String> spliterator = list.spliterator();
        List<String> results = new ArrayList<>();

        while (spliterator.tryAdvance(results::add)) {
            // continue
        }

        assertEquals(Arrays.asList("a", "b", "c"), results);
    }

    @Test
    void testForEachRemaining() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c", "d", "e")
        );

        Spliterator<String> spliterator = list.spliterator();
        List<String> results = new ArrayList<>();

        // Advance once
        spliterator.tryAdvance(results::add);

        // Then use forEachRemaining
        spliterator.forEachRemaining(results::add);

        assertEquals(Arrays.asList("a", "b", "c", "d", "e"), results);
    }

    @Test
    void testTrySplit() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        Spliterator<Integer> spliterator = list.spliterator();
        Spliterator<Integer> split = spliterator.trySplit();

        assertNotNull(split);

        // Collect elements from both spliterators
        List<Integer> fromSplit = new ArrayList<>();
        split.forEachRemaining(fromSplit::add);

        List<Integer> fromOriginal = new ArrayList<>();
        spliterator.forEachRemaining(fromOriginal::add);

        // Verify split sizes
        assertTrue(fromSplit.size() > 0);
        assertTrue(fromOriginal.size() > 0);
        assertEquals(100, fromSplit.size() + fromOriginal.size());

        // Verify all elements present
        Set<Integer> allElements = new HashSet<>(fromSplit);
        allElements.addAll(fromOriginal);
        assertEquals(100, allElements.size());
    }

    @Test
    void testTrySplitEmpty() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>();
        Spliterator<String> spliterator = list.spliterator();

        assertNull(spliterator.trySplit());
    }

    @Test
    void testTrySplitSingleElement() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>();
        list.add("only");

        Spliterator<String> spliterator = list.spliterator();
        assertNull(spliterator.trySplit());
    }

    @Test
    void testSequentialStream() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        List<Integer> result = list.stream()
                .filter(n -> n % 2 == 0)
                .map(n -> n * 2)
                .collect(Collectors.toList());

        assertEquals(50, result.size());
        assertEquals(0, result.get(0));
        assertEquals(196, result.get(49));
    }

    @Test
    void testParallelStream() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }

        long sum = list.parallelStream()
                .mapToInt(Integer::intValue)
                .sum();

        assertEquals(999 * 1000 / 2, sum);
    }

    @Test
    void testStreamCollect() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
                Arrays.asList("apple", "banana", "cherry")
        );

        String result = list.stream()
                .collect(Collectors.joining(", "));

        assertEquals("apple, banana, cherry", result);
    }

    @Test
    void testStreamCount() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 50; i++) {
            list.add(i);
        }

        long count = list.stream().count();
        assertEquals(50, count);
    }

    @Test
    void testStreamFindFirst() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
                Arrays.asList("first", "second", "third")
        );

        Optional<String> result = list.stream().findFirst();

        assertTrue(result.isPresent());
        assertEquals("first", result.get());
    }

    @Test
    void testStreamAnyMatch() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>(
                Arrays.asList(1, 2, 3, 4, 5)
        );

        assertTrue(list.stream().anyMatch(n -> n > 3));
        assertFalse(list.stream().anyMatch(n -> n > 10));
    }

    @Test
    void testConcurrentModificationDuringSplit() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
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

    @Test
    void testConcurrentModificationDuringTraversal() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
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

    @Test
    void testForEachOptimization() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        AtomicInteger sum = new AtomicInteger(0);
        list.forEach(sum::addAndGet);

        assertEquals(4950, sum.get());
    }

    @Test
    void testForEachConcurrentModification() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }

        assertThrows(ConcurrentModificationException.class, () -> {
            list.forEach(x -> list.add(999));
        });
    }

    @Test
    void testStreamOnEmptyList() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>();

        long count = list.stream().count();
        assertEquals(0, count);

        Optional<String> first = list.stream().findFirst();
        assertFalse(first.isPresent());
    }

    @Test
    void testParallelStreamOnSmallList() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>(
                Arrays.asList(1, 2, 3)
        );

        int sum = list.parallelStream()
                .mapToInt(Integer::intValue)
                .sum();

        assertEquals(6, sum);
    }

    @Test
    void testStreamLargeList() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 10000; i++) {
            list.add(i);
        }

        long count = list.stream()
                .filter(n -> n % 100 == 0)
                .count();

        assertEquals(100, count);
    }

    @Test
    void testMultipleSplits() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }

        Spliterator<Integer> s1 = list.spliterator();
        Spliterator<Integer> s2 = s1.trySplit();
        assertNotNull(s2);

        Spliterator<Integer> s3 = s1.trySplit();
        assertNotNull(s3);

        Spliterator<Integer> s4 = s2.trySplit();
        assertNotNull(s4);

        // Collect from all splits
        List<Integer> all = new ArrayList<>();
        s1.forEachRemaining(all::add);
        s2.forEachRemaining(all::add);
        s3.forEachRemaining(all::add);
        s4.forEachRemaining(all::add);

        assertEquals(1000, all.size());
        assertEquals(1000, new HashSet<>(all).size()); // All unique
    }

    @Test
    void testSpliteratorWithNulls() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
                Arrays.asList("a", null, "b", null, "c")
        );

        List<String> result = list.stream()
                .collect(Collectors.toList());

        assertEquals(5, result.size());
        assertEquals("a", result.get(0));
        assertNull(result.get(1));
        assertEquals("b", result.get(2));
        assertNull(result.get(3));
        assertEquals("c", result.get(4));
    }

    @Test
    void testStreamDistinct() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>(
                Arrays.asList(1, 2, 2, 3, 3, 3, 4, 4, 4, 4)
        );

        List<Integer> distinct = list.stream()
                .distinct()
                .collect(Collectors.toList());

        assertEquals(Arrays.asList(1, 2, 3, 4), distinct);
    }

    @Test
    void testStreamSorted() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>(
                Arrays.asList(5, 2, 8, 1, 9, 3)
        );

        List<Integer> sorted = list.stream()
                .sorted()
                .collect(Collectors.toList());

        assertEquals(Arrays.asList(1, 2, 3, 5, 8, 9), sorted);
    }

    @Test
    void testStreamReduce() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>(
                Arrays.asList(1, 2, 3, 4, 5)
        );

        Optional<Integer> sum = list.stream()
                .reduce(Integer::sum);

        assertTrue(sum.isPresent());
        assertEquals(15, sum.get());
    }
}
