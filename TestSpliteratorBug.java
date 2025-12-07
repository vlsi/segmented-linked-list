import com.github.segmentedlist.SegmentedLinkedList;
import java.util.Spliterator;

public class TestSpliteratorBug {
    public static void main(String[] args) {
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(i);
        }

        System.out.println("Starting split test...");
        Spliterator<Integer> s1 = list.spliterator();
        System.out.println("s1 size: " + s1.estimateSize());

        Spliterator<Integer> s2 = s1.trySplit();
        System.out.println("After first split - s1: " + s1.estimateSize() + ", s2: " + s2.estimateSize());

        Spliterator<Integer> s3 = s1.trySplit();
        System.out.println("After second split on s1 - s1: " + s1.estimateSize() + ", s3: " + s3.estimateSize());

        System.out.println("Test completed successfully!");
    }
}
