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
 * Comprehensive test suite for {@link SegmentedLinkedList} using Guava testlib.
 * This generates hundreds of automated tests covering all List contract requirements.
 */
public class SegmentedLinkedListTest {

    public static Test suite() {
        TestSuite suite = new TestSuite("SegmentedLinkedList");
        suite.addTest(createGeneralTestSuite());
        return suite;
    }

    /**
     * Creates a comprehensive test suite for general List operations.
     */
    private static Test createGeneralTestSuite() {
        return ListTestSuiteBuilder
                .using(new TestStringListGenerator() {
                    @Override
                    protected List<String> create(String[] elements) {
                        SegmentedLinkedList<String> list = new SegmentedLinkedList<>();
                        for (String element : elements) {
                            list.add(element);
                        }
                        return list;
                    }
                })
                .named("SegmentedLinkedList")
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
