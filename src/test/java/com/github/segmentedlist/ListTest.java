package com.github.segmentedlist;

import com.google.common.collect.testing.ListTestSuiteBuilder;
import com.google.common.collect.testing.TestStringListGenerator;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.ListFeature;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.List;

/**
 * Comparison test suites for multiple List implementations using Guava testlib.
 * For each {@link Implementation}, this generates a suite covering the List contract,
 * allowing side-by-side verification (e.g., {@link SegmentedLinkedList}, {@link java.util.LinkedList}, etc.).
 */
public class ListTest {

    public static Test suite() {
        TestSuite suite = new TestSuite("Guava list tests");
        for (Implementation value : Implementation.values()) {
            TestedListProvider provider = new TestedListProvider(value);
            suite.addTest(createGeneralTestSuite(provider));
        }
        return suite;
    }

    /**
     * Creates a comprehensive test suite for general List operations.
     */
    private static Test createGeneralTestSuite(TestedListProvider provider) {
        return ListTestSuiteBuilder
                .using(new TestStringListGenerator() {
                    @Override
                    protected List<String> create(String[] elements) {
                        List<String> list = provider.getList();
                        for (String element : elements) {
                            list.add(element);
                        }
                        return list;
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
