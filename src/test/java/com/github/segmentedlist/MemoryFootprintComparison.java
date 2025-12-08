package com.github.segmentedlist;

import org.openjdk.jol.info.GraphLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Compares memory footprint of ArrayList, LinkedList, and SegmentedLinkedList
 * using JOL (Java Object Layout) tool.
 */
public class MemoryFootprintComparison {

    public static void main(String[] args) {
        System.out.println("Memory Footprint Comparison: ArrayList vs LinkedList vs SegmentedLinkedList");
        System.out.println("================================================================================");
        System.out.println();

        // Print table header
        System.out.printf("%10s %12s %12s %12s %-15s %-14s%n",
                "Elements", "ArrayList", "LinkedList", "SegmentedList",
                "SL vs AL (%)", "SL vs LL (%)");
        System.out.println("-".repeat(120));

        // Test with 1 to 100 elements
        for (int size = 0; size <= 100; size++) {
            long arrayListSize = measureArrayList(size);
            long linkedListSize = measureLinkedList(size);
            long segmentedListSize = measureSegmentedList(size);

            double vsArrayList = ((double) segmentedListSize / arrayListSize - 1) * 100;
            double vsLinkedList = ((double) segmentedListSize / linkedListSize - 1) * 100;

            System.out.printf("%10d %12d %12d %12d %+12.1f%% %+13.1f%%%n",
                    size, arrayListSize, linkedListSize, segmentedListSize,
                    vsArrayList, vsLinkedList);
        }

        System.out.println();
        System.out.println("Summary Statistics:");
        System.out.println("-".repeat(120));

        // Calculate and display statistics for key sizes
        int[] keySizes = {0, 1, 2, 10, 50, 100};
        System.out.println();
        System.out.printf("%-10s %-20s %-20s %-20s%n",
                "Elements", "ArrayList (bytes)", "LinkedList (bytes)", "SegmentedList (bytes)");
        System.out.println("-".repeat(75));

        for (int size : keySizes) {
            long arrayListSize = measureArrayList(size);
            long linkedListSize = measureLinkedList(size);
            long segmentedListSize = measureSegmentedList(size);

            System.out.printf("%-10d %-20d %-20d %-20d%n",
                    size, arrayListSize, linkedListSize, segmentedListSize);
        }

        System.out.println();
        System.out.println("Memory per element (approximate):");
        System.out.println("-".repeat(75));

        for (int size : keySizes) {
            long alSize = measureArrayList(size);
            long llSize = measureLinkedList(size);
            long slSize = measureSegmentedList(size);

            long alEmpty = measureArrayList(0);
            long llEmpty = measureLinkedList(0);
            long slEmpty = measureSegmentedList(0);

            double alPerElement = (double) (alSize - alEmpty) / size;
            double llPerElement = (double) (llSize - llEmpty) / size;
            double slPerElement = (double) (slSize - slEmpty) / size;

            System.out.printf("At %d elements: AL=%.1f, LL=%.1f, SL=%.1f bytes/element%n",
                    size, alPerElement, llPerElement, slPerElement);
        }
    }

    private static long measureArrayList(int size) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add("");
        }
        return GraphLayout.parseInstance(list).totalSize();
    }

    private static long measureLinkedList(int size) {
        List<String> list = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add("");
        }
        return GraphLayout.parseInstance(list).totalSize();
    }

    private static long measureSegmentedList(int size) {
        List<String> list = new SegmentedLinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add("");
        }
        return GraphLayout.parseInstance(list).totalSize();
    }
}
