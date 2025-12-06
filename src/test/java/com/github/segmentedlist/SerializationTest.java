package com.github.segmentedlist;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for serialization and cloning functionality of {@link SegmentedLinkedList}.
 */
class SerializationTest {

    @Test
    void testSerializationEmpty() throws Exception {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>();
        SegmentedLinkedList<String> deserialized = serializeAndDeserialize(original);

        assertEquals(original.size(), deserialized.size());
        assertTrue(deserialized.isEmpty());
    }

    @Test
    void testSerializationSingleElement() throws Exception {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>();
        original.add("test");

        SegmentedLinkedList<String> deserialized = serializeAndDeserialize(original);

        assertEquals(original.size(), deserialized.size());
        assertEquals(original, deserialized);
    }

    @Test
    void testSerializationMultipleSegments() throws Exception {
        SegmentedLinkedList<Integer> original = new SegmentedLinkedList<>();
        // Add enough elements to span multiple segments (16 elements per segment)
        for (int i = 0; i < 50; i++) {
            original.add(i);
        }

        SegmentedLinkedList<Integer> deserialized = serializeAndDeserialize(original);

        assertEquals(original.size(), deserialized.size());
        assertEquals(original, deserialized);
    }

    @Test
    void testSerializationWithNulls() throws Exception {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>();
        original.add("first");
        original.add(null);
        original.add("third");
        original.add(null);

        SegmentedLinkedList<String> deserialized = serializeAndDeserialize(original);

        assertEquals(original.size(), deserialized.size());
        assertEquals(original, deserialized);
        assertNull(deserialized.get(1));
        assertNull(deserialized.get(3));
    }

    @Test
    void testCloneEmpty() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>();
        SegmentedLinkedList<String> clone = original.clone();

        assertEquals(original, clone);
        assertNotSame(original, clone);
    }

    @Test
    void testCloneSingleElement() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>();
        original.add("test");

        SegmentedLinkedList<String> clone = original.clone();

        assertEquals(original, clone);
        assertNotSame(original, clone);

        // Verify independence
        clone.add("additional");
        assertNotEquals(original.size(), clone.size());
    }

    @Test
    void testCloneMultipleSegments() {
        SegmentedLinkedList<Integer> original = new SegmentedLinkedList<>();
        for (int i = 0; i < 50; i++) {
            original.add(i);
        }

        SegmentedLinkedList<Integer> clone = original.clone();

        assertEquals(original, clone);
        assertNotSame(original, clone);

        // Verify independence
        clone.set(25, 999);
        assertNotEquals(original.get(25), clone.get(25));
    }

    @Test
    void testCloneWithNulls() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>();
        original.add("first");
        original.add(null);
        original.add("third");

        SegmentedLinkedList<String> clone = original.clone();

        assertEquals(original, clone);
        assertNull(clone.get(1));

        // Verify independence
        clone.set(1, "modified");
        assertNull(original.get(1));
        assertEquals("modified", clone.get(1));
    }

    @Test
    void testCloneIndependence() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c", "d", "e")
        );

        SegmentedLinkedList<String> clone = original.clone();

        // Modify original
        original.add("f");
        original.remove(0);
        original.set(1, "modified");

        // Clone should be unchanged
        assertEquals(5, clone.size());
        assertEquals("a", clone.get(0));
        assertEquals("c", clone.get(2));
    }

    @Test
    void testSerializationPreservesOrder() throws Exception {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>();
        for (int i = 0; i < 100; i++) {
            original.add("element-" + i);
        }

        SegmentedLinkedList<String> deserialized = serializeAndDeserialize(original);

        assertEquals(original.size(), deserialized.size());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i), deserialized.get(i),
                    "Mismatch at index " + i);
        }
    }

    @Test
    void testSerializationAfterDequeOperations() throws Exception {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>();
        original.addFirst("first");
        original.addLast("last");
        original.addFirst("new-first");
        original.addLast("new-last");

        SegmentedLinkedList<String> deserialized = serializeAndDeserialize(original);

        assertEquals(original.size(), deserialized.size());
        assertEquals("new-first", deserialized.getFirst());
        assertEquals("new-last", deserialized.getLast());
        assertEquals(original, deserialized);
    }

    @Test
    void testCloneReversedView() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c", "d", "e")
        );

        SegmentedLinkedList<String> reversed = original.reversed();
        SegmentedLinkedList<String> clone = reversed.clone();

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

    @Test
    void testCloneReversedViewEmpty() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>();
        SegmentedLinkedList<String> reversed = original.reversed();
        SegmentedLinkedList<String> clone = reversed.clone();

        assertTrue(clone.isEmpty());
        assertNotSame(reversed, clone);

        // Verify independence
        clone.add("test");
        assertTrue(reversed.isEmpty());
        assertEquals(1, clone.size());
    }

    @Test
    void testCloneReversedViewMultipleSegments() {
        SegmentedLinkedList<Integer> original = new SegmentedLinkedList<>();
        // Add enough elements to span multiple segments (16 elements per segment)
        for (int i = 0; i < 50; i++) {
            original.add(i);
        }

        SegmentedLinkedList<Integer> reversed = original.reversed();
        SegmentedLinkedList<Integer> clone = reversed.clone();

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

    @Test
    void testCloneReversedViewDoubleReverse() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c")
        );

        SegmentedLinkedList<String> reversed = original.reversed();
        SegmentedLinkedList<String> doubleReversed = reversed.reversed();
        SegmentedLinkedList<String> clone = doubleReversed.clone();

        // Double reverse should give original order
        assertEquals("a", clone.get(0));
        assertEquals("b", clone.get(1));
        assertEquals("c", clone.get(2));

        // Should be independent
        clone.add("d");
        assertEquals(3, original.size());
        assertEquals(4, clone.size());
    }

    @Test
    void testCloneReversedViewPreservesReversedBehavior() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("first", "second", "third")
        );

        SegmentedLinkedList<String> reversed = original.reversed();
        SegmentedLinkedList<String> clone = reversed.clone();

        // Clone should behave as a reversed view
        clone.addFirst("new-first"); // This adds to the "reversed" first position
        assertEquals("new-first", clone.get(0));
        assertEquals("third", clone.get(1));

        // Original should be independent
        assertEquals(3, original.size());
        assertEquals(4, clone.size());
    }

    @Test
    void testSerializeReversedView_ThrowsException() {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c")
        );
        SegmentedLinkedList<String> reversed = original.reversed();

        // Attempting to serialize a reversed view should throw InvalidObjectException
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertThrows(java.io.InvalidObjectException.class, () -> {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(reversed);
            }
        });
    }

    @Test
    void testSerializeReversedView_ForwardListWorks() throws Exception {
        SegmentedLinkedList<String> original = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c")
        );

        // Create reversed view
        SegmentedLinkedList<String> reversed = original.reversed();

        // Reversed view cannot be serialized
        assertThrows(java.io.InvalidObjectException.class, () -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(reversed);
            }
        });

        // But original list can be serialized normally
        SegmentedLinkedList<String> deserialized = serializeAndDeserialize(original);
        assertEquals(original, deserialized);
        assertEquals("a", deserialized.get(0));
        assertEquals("b", deserialized.get(1));
        assertEquals("c", deserialized.get(2));
    }

    @SuppressWarnings("unchecked")
    private <E> SegmentedLinkedList<E> serializeAndDeserialize(SegmentedLinkedList<E> list)
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(list);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (SegmentedLinkedList<E>) ois.readObject();
        }
    }
}
