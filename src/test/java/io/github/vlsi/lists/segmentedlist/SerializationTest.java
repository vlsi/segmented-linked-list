package io.github.vlsi.lists.segmentedlist;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comparison tests for serialization and cloning functionality across multiple List implementations.
 * Executed side-by-side via {@link org.junit.jupiter.api.TestTemplate} and {@link ExtendWith}
 * with {@link ComparisonTestExtension}, using {@link TestedListProvider} to supply the concrete
 * implementation under test (e.g., {@link SegmentedLinkedList}, {@link java.util.LinkedList}, or others).
 */
@ExtendWith(ComparisonTestExtension.class)
class SerializationTest {

    @TestTemplate
    void testSerializationEmpty(TestedListProvider provider) throws Exception {
        List<String> original = provider.getList();
        List<String> deserialized = serializeAndDeserialize(original);

        assertEquals(original.size(), deserialized.size());
        assertTrue(deserialized.isEmpty());
    }

    @TestTemplate
    void testSerializationSingleElement(TestedListProvider provider) throws Exception {
        List<String> original = provider.getList();
        original.add("test");

        List<String> deserialized = serializeAndDeserialize(original);

        assertEquals(original.size(), deserialized.size());
        assertEquals(original, deserialized);
    }

    @TestTemplate
    void testSerializationMultipleSegments(TestedListProvider provider) throws Exception {
        List<Integer> original = provider.getList();
        // Add enough elements to span multiple segments (16 elements per segment)
        for (int i = 0; i < 50; i++) {
            original.add(i);
        }

        List<Integer> deserialized = serializeAndDeserialize(original);

        assertEquals(original.size(), deserialized.size());
        assertEquals(original, deserialized);
    }

    @TestTemplate
    void testSerializationWithNulls(TestedListProvider provider) throws Exception {
        List<String> original = provider.getList();
        original.add("first");
        original.add(null);
        original.add("third");
        original.add(null);

        List<String> deserialized = serializeAndDeserialize(original);

        assertEquals(original.size(), deserialized.size());
        assertEquals(original, deserialized);
        assertNull(deserialized.get(1));
        assertNull(deserialized.get(3));
    }

    @TestTemplate
    void testCloneEmpty(TestedListProvider provider) {
        List<String> original = provider.getList();
        List<String> clone = provider.clone(original);

        assertEquals(original, clone);
        assertNotSame(original, clone);
    }

    @TestTemplate
    void testCloneSingleElement(TestedListProvider provider) {
        List<String> original = provider.getList();
        original.add("test");

        List<String> clone = provider.clone(original);

        assertEquals(original, clone);
        assertNotSame(original, clone);

        // Verify independence
        clone.add("additional");
        assertNotEquals(original.size(), clone.size());
    }

    @TestTemplate
    void testCloneMultipleSegments(TestedListProvider provider) {
        List<Integer> original = provider.getList();
        for (int i = 0; i < 50; i++) {
            original.add(i);
        }

        List<Integer> clone = provider.clone(original);

        assertEquals(original, clone);
        assertNotSame(original, clone);

        // Verify independence
        clone.set(25, 999);
        assertNotEquals(original.get(25), clone.get(25));
    }

    @TestTemplate
    void testCloneWithNulls(TestedListProvider provider) {
        List<String> original = provider.getList();
        original.add("first");
        original.add(null);
        original.add("third");

        List<String> clone = provider.clone(original);

        assertEquals(original, clone);
        assertNull(clone.get(1));

        // Verify independence
        clone.set(1, "modified");
        assertNull(original.get(1));
        assertEquals("modified", clone.get(1));
    }

    @TestTemplate
    void testCloneIndependence(TestedListProvider provider) {
        List<String> original = provider.getList(
                Arrays.asList("a", "b", "c", "d", "e")
        );

        List<String> clone = provider.clone(original);

        // Modify original
        original.add("f");
        original.remove(0);
        original.set(1, "modified");

        // Clone should be unchanged
        assertEquals(5, clone.size());
        assertEquals("a", clone.get(0));
        assertEquals("c", clone.get(2));
    }

    @TestTemplate
    void testSerializationPreservesOrder(TestedListProvider provider) throws Exception {
        List<String> original = provider.getList();
        for (int i = 0; i < 100; i++) {
            original.add("element-" + i);
        }

        List<String> deserialized = serializeAndDeserialize(original);

        assertEquals(original.size(), deserialized.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i), deserialized.get(i),
                    "Mismatch at index " + i);
        }
    }

    @TestTemplate
    void testSerializationAfterDequeOperations(TestedListProvider provider) throws Exception {
        List<String> original = provider.getList();
        original.addFirst("first");
        original.addLast("last");
        original.addFirst("new-first");
        original.addLast("new-last");

        List<String> deserialized = serializeAndDeserialize(original);

        assertEquals(original.size(), deserialized.size());
        assertEquals("new-first", deserialized.getFirst());
        assertEquals("new-last", deserialized.getLast());
        assertEquals(original, deserialized);
    }

    @TestTemplate
    void testCloneReversedView(TestedListProvider provider) {
        Assumptions.assumeTrue(provider.implementation() == Implementation.SEGMENTED);
        List<String> original = provider.getList(
                Arrays.asList("a", "b", "c", "d", "e")
        );

        List<String> reversed = original.reversed();
        List<String> clone = provider.clone(reversed);

        // Clone should be a reversed view with correct order
        assertEquals(5, clone.size());
        assertEquals("e", clone.get(0));
        assertEquals("d", clone.get(1));
        assertEquals("c", clone.get(2));
        assertEquals("b", clone.get(3));
        assertEquals("a", clone.get(4));

        // Clone should not be the same object
        assertNotSame(reversed, clone);

        // Clone should be independent - modifying clone shouldn't affect reversed view
        clone.add("f");
        assertEquals(5, reversed.size());
        assertEquals(6, clone.size());

        // Original should still be unchanged
        assertEquals(5, original.size());
        assertEquals("a", original.get(0));
    }

    @TestTemplate
    void testCloneReversedViewEmpty(TestedListProvider provider) {
        Assumptions.assumeTrue(provider.implementation() == Implementation.SEGMENTED);
        List<String> original = provider.getList();
        List<String> reversed = original.reversed();
        List<String> clone = provider.clone(reversed);

        assertTrue(clone.isEmpty());
        assertNotSame(reversed, clone);

        // Verify independence
        clone.add("test");
        assertTrue(reversed.isEmpty());
        assertEquals(1, clone.size());
    }

    @TestTemplate
    void testCloneReversedViewMultipleSegments(TestedListProvider provider) {
        Assumptions.assumeTrue(provider.implementation() == Implementation.SEGMENTED);

        List<Integer> original = provider.getList();
        // Add enough elements to span multiple segments (16 elements per segment)
        for (int i = 0; i < 50; i++) {
            original.add(i);
        }

        List<Integer> reversed = original.reversed();
        List<Integer> clone = provider.clone(reversed);

        // Verify reversed order
        assertEquals(50, clone.size());
        assertEquals(49, clone.get(0));
        assertEquals(48, clone.get(1));
        assertEquals(0, clone.get(49));

        // Verify independence
        clone.set(0, 999);
        assertEquals(49, reversed.get(0));
        assertEquals(999, clone.get(0));

        // Original should be unchanged
        assertEquals(49, original.get(49));
    }

    @TestTemplate
    void testCloneReversedViewDoubleReverse(TestedListProvider provider) {
        List<String> original = provider.getList(
                Arrays.asList("a", "b", "c")
        );

        List<String> reversed = original.reversed();
        List<String> doubleReversed = reversed.reversed();
        List<String> clone = provider.clone(doubleReversed);

        // Double reverse should give original order
        assertEquals("a", clone.get(0));
        assertEquals("b", clone.get(1));
        assertEquals("c", clone.get(2));

        // Should be independent
        clone.add("d");
        assertEquals(3, original.size());
        assertEquals(4, clone.size());
    }

    @TestTemplate
    void testCloneReversedViewPreservesReversedBehavior(TestedListProvider provider) {
        Assumptions.assumeTrue(provider.implementation() == Implementation.SEGMENTED);
        List<String> original = provider.getList(
                Arrays.asList("first", "second", "third")
        );

        List<String> reversed = original.reversed();
        List<String> clone = provider.clone(reversed);

        // Clone should behave as a reversed view
        clone.addFirst("new-first"); // This adds to the "reversed" first position
        assertEquals("new-first", clone.get(0));
        assertEquals("third", clone.get(1));

        // Original should be independent
        assertEquals(3, original.size());
        assertEquals(4, clone.size());
    }

    @TestTemplate
    void testSerializeReversedView_ThrowsException(TestedListProvider provider) {
        List<String> original = provider.getList(
                Arrays.asList("a", "b", "c")
        );
        List<String> reversed = original.reversed();

        // Attempting to serialize a reversed view should throw InvalidObjectException
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertThrows(java.io.ObjectStreamException.class, () -> {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(reversed);
            }
        });
    }

    @TestTemplate
    void testSerializeReversedView_ForwardListWorks(TestedListProvider provider) throws Exception {
        List<String> original = provider.getList(
                Arrays.asList("a", "b", "c")
        );

        // Create reversed view
        List<String> reversed = original.reversed();

        // Reversed view cannot be serialized
        assertThrows(java.io.ObjectStreamException.class, () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(reversed);
            }
        });

        // But original list can be serialized normally
        List<String> deserialized = serializeAndDeserialize(original);
        assertEquals(original, deserialized);
        assertEquals("a", deserialized.get(0));
        assertEquals("b", deserialized.get(1));
        assertEquals("c", deserialized.get(2));
    }

    @SuppressWarnings("unchecked")
    private <E> List<E> serializeAndDeserialize(List<E> list)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(list);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (List<E>) ois.readObject();
        }
    }
}
