package io.github.vlsi.lists.segmentedlist;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import io.github.vlsi.lists.arraybackedlist.ArrayBackedLinkedList;
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
    private List<Integer> arrayBackedList;

    @Setup(Level.Iteration)
    public void setup() {
        segmentedList = new SegmentedLinkedList<>();
        linkedList = new LinkedList<>();
        arrayList = new ArrayList<>();
        arrayBackedList = new ArrayBackedLinkedList<>();

        for (int i = 0; i < size; i++) {
            segmentedList.add(i);
            linkedList.add(i);
            arrayList.add(i);
            arrayBackedList.add(i);
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

    @Benchmark
    public List<Integer> addLast_ArrayBackedLinkedList() {
        List<Integer> list = new ArrayBackedLinkedList<>();
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

    @Benchmark
    public List<Integer> addFirst_ArrayBackedLinkedList() {
        ArrayBackedLinkedList<Integer> list = new ArrayBackedLinkedList<>();
        for (int i = 0; i < size; i++) {
            list.addFirst(i);
        }
        return list;
    }

    // ==================== GET BY INDEX BENCHMARKS ====================

    @Benchmark
    public int getByIndex_SegmentedList() {
        int sum = 0;
        for (int i = 0; i < size; i++) {
            sum += segmentedList.get(i);
        }
        return sum;
    }

    @Benchmark
    public int getByIndex_LinkedList() {
        int sum = 0;
        for (int i = 0; i < size; i++) {
            sum += linkedList.get(i);
        }
        return sum;
    }

    @Benchmark
    public int getByIndex_ArrayList() {
        int sum = 0;
        for (int i = 0; i < size; i++) {
            sum += arrayList.get(i);
        }
        return sum;
    }

    @Benchmark
    public int getByIndex_ArrayBackedLinkedList() {
        int sum = 0;
        for (int i = 0; i < size; i++) {
            sum += arrayBackedList.get(i);
        }
        return sum;
    }

    // ==================== ITERATION BENCHMARKS ====================

    @Benchmark
    public void iterate_SegmentedList(Blackhole blackhole) {
        for (Integer value : segmentedList) {
            blackhole.consume(value);
        }
    }

    @Benchmark
    public void iterate_LinkedList(Blackhole blackhole) {
        for (Integer value : linkedList) {
            blackhole.consume(value);
        }
    }

    @Benchmark
    public void iterate_ArrayList(Blackhole blackhole) {
        for (Integer value : arrayList) {
            blackhole.consume(value);
        }
    }

    @Benchmark
    public void iterate_ArrayBackedLinkedList(Blackhole blackhole) {
        for (Integer value : arrayBackedList) {
            blackhole.consume(value);
        }
    }

    // ==================== REMOVE BY INDEX BENCHMARKS ====================

    @Benchmark
    public List<Integer> removeByIndex_SegmentedList() {
        List<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        for (int i = size - 1; i >= 0; i--) {
            list.remove(i);
        }
        return list;
    }

    @Benchmark
    public List<Integer> removeByIndex_LinkedList() {
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        for (int i = size - 1; i >= 0; i--) {
            list.remove(i);
        }
        return list;
    }

    @Benchmark
    public List<Integer> removeByIndex_ArrayList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        for (int i = size - 1; i >= 0; i--) {
            list.remove(i);
        }
        return list;
    }

    @Benchmark
    public List<Integer> removeByIndex_ArrayBackedLinkedList() {
        List<Integer> list = new ArrayBackedLinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        for (int i = size - 1; i >= 0; i--) {
            list.remove(i);
        }
        return list;
    }

    // ==================== ADD AT INDEX BENCHMARKS ====================

    @Benchmark
    public List<Integer> addAtIndex_SegmentedList() {
        List<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        for (int i = 0; i < size; i++) {
            list.add(i, -i);
        }
        return list;
    }

    @Benchmark
    public List<Integer> addAtIndex_LinkedList() {
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        for (int i = 0; i < size; i++) {
            list.add(i, -i);
        }
        return list;
    }

    @Benchmark
    public List<Integer> addAtIndex_ArrayList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        for (int i = 0; i < size; i++) {
            list.add(i, -i);
        }
        return list;
    }

    @Benchmark
    public List<Integer> addAtIndex_ArrayBackedLinkedList() {
        List<Integer> list = new ArrayBackedLinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        for (int i = 0; i < size; i++) {
            list.add(i, -i);
        }
        return list;
    }

    // ==================== REMOVE BY VALUE BENCHMARKS ====================

    @Benchmark
    public List<Integer> removeByValue_SegmentedList() {
        List<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        for (int i = 0; i < size; i++) {
            list.remove(Integer.valueOf(i));
        }
        return list;
    }

    @Benchmark
    public List<Integer> removeByValue_LinkedList() {
        List<Integer> list = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        for (int i = 0; i < size; i++) {
            list.remove(Integer.valueOf(i));
        }
        return list;
    }

    @Benchmark
    public List<Integer> removeByValue_ArrayList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        for (int i = 0; i < size; i++) {
            list.remove(Integer.valueOf(i));
        }
        return list;
    }

    @Benchmark
    public List<Integer> removeByValue_ArrayBackedLinkedList() {
        List<Integer> list = new ArrayBackedLinkedList<>();
        for (int i = 0; i < size; i++) {
            list.add(i);
        }
        for (int i = 0; i < size; i++) {
            list.remove(Integer.valueOf(i));
        }
        return list;
    }
}
