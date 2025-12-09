package com.github.segmentedlist;

import com.google.common.collect.testing.*;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.Deque;
import java.util.List;
import java.util.Queue;

/**
 * Comparison test suites for multiple List implementations using Guava testlib.
 * For each {@link Implementation}, this generates a suite covering the List contract,
 * allowing side-by-side verification (e.g., {@link SegmentedLinkedList}, {@link java.util.LinkedList}, etc.).
 */
public class QueueTest {

    public static Test suite() {
        TestSuite suite = new TestSuite("Guava queue tests");
        for (Implementation value : Implementation.values()) {
            TestedListProvider provider = new TestedListProvider(value);
            if (provider.isDequeSupported()) {
                suite.addTest(createGeneralTestSuite(provider));
            }
        }
        return suite;
    }

    /**
     * Creates a comprehensive test suite for general List operations.
     */
    private static Test createGeneralTestSuite(TestedListProvider provider) {
        return QueueTestSuiteBuilder
                .using(new TestQueueGenerator<String>() {
                    @Override
                    public SampleElements<String> samples() {
                        return new SampleElements.Strings();
                    }

                    @Override
                    public Queue<String> create(Object... elements) {
                        Deque<String> list = provider.getDeque();
                        for (Object element : elements) {
                            list.add(element == null ? null : element.toString());
                        }
                        return list;
                    }

                    @Override
                    public String[] createArray(int length) {
                        return new String[length];
                    }

                    @Override
                    public Iterable<String> order(List<String> insertionOrder) {
                        return insertionOrder;
                    }
                })

                .named(provider.implementationName())
                .withFeatures(
                        ListFeature.GENERAL_PURPOSE,
                        CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionFeature.ALLOWS_NULL_QUERIES,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        CollectionFeature.SERIALIZABLE,
                        CollectionSize.ANY
                )
                .createTestSuite();
    }
}
