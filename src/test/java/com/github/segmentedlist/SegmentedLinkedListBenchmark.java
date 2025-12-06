package com.github.segmentedlist;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmarks comparing {@link SegmentedLinkedList} performance against
 * standard Java {@link LinkedList} and {@link ArrayList}.
 *
 * <p>Run with: java -jar target/benchmarks.jar</p>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class SegmentedLinkedListBenchmark {

    @Param({"10", "100", "1000", "10000"})
    private int size;

    private List<Integer> segmentedList;
    private List<Integer> linkedList;
    private List<Integer> arrayList;

    @Setup(Level.Iteration)
    public void setup() {
        segmentedList = new SegmentedLinkedList<>();
        linkedList = new LinkedList<>();
        arrayList = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            segmentedList.add(i);
            linkedList.add(i);
            arrayList.add(i);
        }
    }

    // ==================== ADD LAST BENCHMARKS ====================

    @Benchmark
    public List<Integer> addLast_SegmentedList() {
        List<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        return list;
    }

    @Benchmark
    public List<Integer> addLast_LinkedList() {
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        return list;
    }

    @Benchmark
    public List<Integer> addLast_ArrayList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        return list;
    }

    // ==================== ADD FIRST BENCHMARKS ====================

    @Benchmark
    public List<Integer> addFirst_SegmentedList() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < size; i++) {
            list.addFirst(i);
        }
        return list;
    }

    @Benchmark
    public List<Integer> addFirst_LinkedList() {
        LinkedList<Integer> list = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            list.addFirst(i);
        }
        return list;
    }

    @Benchmark
    public List<Integer> addFirst_ArrayList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(0, i);
        }
        return list;
    }

    // ==================== GET BENCHMARKS ====================

    @Benchmark
    public void get_SegmentedList(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(segmentedList.get(i));
        }
    }

    @Benchmark
    public void get_LinkedList(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(linkedList.get(i));
        }
    }

    @Benchmark
    public void get_ArrayList(Blackhole bh) {
        for (int i = 0; i < size; i++) {
            bh.consume(arrayList.get(i));
        }
    }

    // ==================== ITERATOR BENCHMARKS ====================

    @Benchmark
    public void iterate_SegmentedList(Blackhole bh) {
        for (Integer value : segmentedList) {
            bh.consume(value);
        }
    }

    @Benchmark
    public void iterate_LinkedList(Blackhole bh) {
        for (Integer value : linkedList) {
            bh.consume(value);
        }
    }

    @Benchmark
    public void iterate_ArrayList(Blackhole bh) {
        for (Integer value : arrayList) {
            bh.consume(value);
        }
    }

    // ==================== REMOVE FIRST BENCHMARKS ====================

    @Benchmark
    public void removeFirst_SegmentedList() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>(segmentedList);
        while (!list.isEmpty()) {
            list.removeFirst();
        }
    }

    @Benchmark
    public void removeFirst_LinkedList() {
        LinkedList<Integer> list = new LinkedList<>(linkedList);
        while (!list.isEmpty()) {
            list.removeFirst();
        }
    }

    @Benchmark
    public void removeFirst_ArrayList() {
        List<Integer> list = new ArrayList<>(arrayList);
        while (!list.isEmpty()) {
            list.remove(0);
        }
    }

    // ==================== REMOVE LAST BENCHMARKS ====================

    @Benchmark
    public void removeLast_SegmentedList() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>(segmentedList);
        while (!list.isEmpty()) {
            list.removeLast();
        }
    }

    @Benchmark
    public void removeLast_LinkedList() {
        LinkedList<Integer> list = new LinkedList<>(linkedList);
        while (!list.isEmpty()) {
            list.removeLast();
        }
    }

    @Benchmark
    public void removeLast_ArrayList() {
        List<Integer> list = new ArrayList<>(arrayList);
        while (!list.isEmpty()) {
            list.remove(list.size() - 1);
        }
    }

    // ==================== ADD MIDDLE BENCHMARKS ====================

    @Benchmark
    public void addMiddle_SegmentedList() {
        List<Integer> list = new SegmentedLinkedList<>(segmentedList);
        int middle = list.size() / 2;
        list.add(middle, 999);
    }

    @Benchmark
    public void addMiddle_LinkedList() {
        List<Integer> list = new LinkedList<>(linkedList);
        int middle = list.size() / 2;
        list.add(middle, 999);
    }

    @Benchmark
    public void addMiddle_ArrayList() {
        List<Integer> list = new ArrayList<>(arrayList);
        int middle = list.size() / 2;
        list.add(middle, 999);
    }

    // ==================== CONTAINS BENCHMARKS ====================

    @Benchmark
    public boolean contains_SegmentedList() {
        return segmentedList.contains(size / 2);
    }

    @Benchmark
    public boolean contains_LinkedList() {
        return linkedList.contains(size / 2);
    }

    @Benchmark
    public boolean contains_ArrayList() {
        return arrayList.contains(size / 2);
    }

    // ==================== MEMORY FOOTPRINT TEST ====================

    /**
     * Test to compare memory usage (not a performance benchmark).
     * Run separately to measure heap consumption.
     */
    @Benchmark
    @Measurement(iterations = 1)
    public List<Integer> memoryFootprint_SegmentedList() {
        List<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 100000; i++) {
            list.add(i);
        }
        return list;
    }

    @Benchmark
    @Measurement(iterations = 1)
    public List<Integer> memoryFootprint_LinkedList() {
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < 100000; i++) {
            list.add(i);
        }
        return list;
    }

    @Benchmark
    @Measurement(iterations = 1)
    public List<Integer> memoryFootprint_ArrayList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            list.add(i);
        }
        return list;
    }
}
