package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the reversed view functionality of {@link SegmentedLinkedList}.
 * Verifies that reversed() returns an O(1) view, not a copy, and that modifications
 * propagate bidirectionally.
 */
class ReversedViewTest {

    @Test
    void testReversedIsView_NotCopy() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c", "d")
        );

        SegmentedLinkedList<String> reversed = original.reversed();

        // Verify reversed view shows correct order
        assertEquals(4, reversed.size());
        assertEquals("d", reversed.get(0));
        assertEquals("c", reversed.get(1));
        assertEquals("b", reversed.get(2));
        assertEquals("a", reversed.get(3));
    }

    @Test
    void testReversedView_ModificationInViewAffectsOriginal() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c", "d")
        );

        SegmentedLinkedList<String> reversed = original.reversed();

        // Modify via reversed view
        reversed.set(0, "Z"); // Changes last element of original

        // Verify change is visible in original
        assertEquals("Z", original.get(3));
        assertEquals("Z", original.getLast());
    }

    @Test
    void testReversedView_ModificationInOriginalAffectsView() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c", "d")
        );

        SegmentedLinkedList<String> reversed = original.reversed();

        // Modify original
        original.set(0, "X"); // Changes first element

        // Verify change is visible in reversed view (at last position)
        assertEquals("X", reversed.get(3));
        assertEquals("X", reversed.getLast());
    }

    @Test
    void testReversedView_AddToViewAffectsOriginal() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c")
        );

        SegmentedLinkedList<String> reversed = original.reversed();

        // Add to reversed view
        reversed.addFirst("Z"); // Should add to end of original

        assertEquals(4, original.size());
        assertEquals("Z", original.getLast());
        assertEquals("Z", reversed.getFirst());
    }

    @Test
    void testReversedView_RemoveFromViewAffectsOriginal() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c", "d")
        );

        SegmentedLinkedList<String> reversed = original.reversed();

        // Remove from reversed view
        String removed = reversed.removeFirst(); // Removes last from original

        assertEquals("d", removed);
        assertEquals(3, original.size());
        assertEquals("c", original.getLast());
    }

    @Test
    void testReversedView_ClearInViewClearsOriginal() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c", "d")
        );

        SegmentedLinkedList<String> reversed = original.reversed();

        reversed.clear();

        assertTrue(original.isEmpty());
        assertTrue(reversed.isEmpty());
    }

    @Test
    void testReversedView_IteratorModifiesBothDirections() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c")
        );

        SegmentedLinkedList<String> reversed = original.reversed();

        // Iterate and modify via reversed view
        // Reversed view shows: [c, b, a]
        var it = reversed.listIterator();
        while (it.hasNext()) {
            String value = it.next();
            it.set(value.toUpperCase());
        }

        // Verify changes in original - the reversed view iterates c→b→a
        // so original should become [A, B, C] (reversed order of iteration)
        assertEquals("A", original.get(0));
        assertEquals("B", original.get(1));
        assertEquals("C", original.get(2));
    }

    @Test
    void testDoubleReversed_ReturnsOriginal() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c")
        );

        SegmentedLinkedList<String> reversed = original.reversed();
        SegmentedLinkedList<String> doubleReversed = reversed.reversed();

        // Double reversed should return the original list
        assertSame(original, doubleReversed);
    }

    @Test
    void testReversedView_DequeOperations() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c")
        );

        SegmentedLinkedList<String> reversed = original.reversed();

        // addFirst on reversed = addLast on original
        reversed.addFirst("Z");
        assertEquals("Z", original.getLast());

        // addLast on reversed = addFirst on original
        reversed.addLast("Y");
        assertEquals("Y", original.getFirst());

        // removeFirst on reversed = removeLast on original
        String removed = reversed.removeFirst();
        assertEquals("Z", removed);
        assertEquals("c", original.getLast());

        // removeLast on reversed = removeFirst on original
        removed = reversed.removeLast();
        assertEquals("Y", removed);
        assertEquals("a", original.getFirst());
    }

    @Test
    void testReversedView_DescendingIteratorIsForwardIterator() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c", "d")
        );

        SegmentedLinkedList<String> reversed = original.reversed();

        // descendingIterator on reversed view should iterate like forward iterator on original
        var it = reversed.descendingIterator();
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertEquals("d", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    void testReversedView_RemoveFirstOccurrence() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c", "b", "d")
        );

        SegmentedLinkedList<String> reversed = original.reversed();

        // removeFirstOccurrence on reversed = removeLastOccurrence on original
        boolean removed = reversed.removeFirstOccurrence("b");

        assertTrue(removed);
        assertEquals(Arrays.asList("a", "b", "c", "d"), original);
    }

    @Test
    void testReversedView_RemoveLastOccurrence() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c", "b", "d")
        );

        SegmentedLinkedList<String> reversed = original.reversed();

        // removeLastOccurrence on reversed = removeFirstOccurrence on original
        // Reversed view shows: [d, b, c, b, a]
        // Last occurrence of "b" in reversed is at index 3 (which is index 1 in original)
        boolean removed = reversed.removeLastOccurrence("b");

        assertTrue(removed);
        assertEquals(Arrays.asList("a", "c", "b", "d"), original);
    }

    @Test
    void testReversedView_LargeList() {
        SegmentedLinkedList<Integer> original = new SegmentedLinkedList<>();
        // Add 100 elements (spans multiple segments)
        for (int i = 0; i < 100; i++) {
            original.add(i);
        }

        SegmentedLinkedList<Integer> reversed = original.reversed();

        // Verify reversed order
        for (int i = 0; i < 100; i++) {
            assertEquals(99 - i, reversed.get(i));
        }

        // Modify via reversed view
        reversed.set(0, -1);
        assertEquals(-1, original.get(99));
    }

    @Test
    void testReversedView_EmptyList() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>();
        SegmentedLinkedList<String> reversed = original.reversed();

        assertTrue(reversed.isEmpty());
        assertEquals(0, reversed.size());

        // Add to reversed view of empty list
        reversed.add("test");
        assertEquals(1, original.size());
        assertEquals("test", original.get(0));
    }
}
