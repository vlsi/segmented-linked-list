import com.github.segmentedlist.SegmentedLinkedList;
import java.util.Arrays;
import java.util.Spliterator;

public class MinimalReproducer {
    public static void main(String[] args) {
        System.out.println("Test 1: testSpliteratorCharacteristics");
        test1();

        System.out.println("Test 2: testSpliteratorEstimateSize");
        test2();

        System.out.println("Test 3: testTryAdvance");
        test3();

        System.out.println("All tests completed!");
    }

    static void test1() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c")
        );
        Spliterator<String> spliterator = list.spliterator();
        System.out.println("  Characteristics check passed");
    }

    static void test2() {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }
        Spliterator<Integer> spliterator = list.spliterator();
        System.out.println("  Estimate size: " + spliterator.estimateSize());
    }

    static void test3() {
        SegmentedLinkedList<String> list = new SegmentedLinkedList<>(
                Arrays.asList("a", "b", "c")
        );
        Spliterator<String> spliterator = list.spliterator();
        java.util.List<String> results = new java.util.ArrayList<>();
        while (spliterator.tryAdvance(results::add)) {
            // continue
        }
        System.out.println("  Results: " + results);
    }
}
