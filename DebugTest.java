import com.github.segmentedlist.SegmentedLinkedList;
import java.util.*;
import java.util.stream.Collectors;

public class DebugTest {
    public static void main(String[] args) {
        System.out.println("Creating list with 100 elements...");
        SegmentedLinkedList<Integer> list = new SegmentedLinkedList<>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        System.out.println("Testing parallel stream...");
        long start = System.currentTimeMillis();
        long sum = list.parallelStream()
                .mapToInt(Integer::intValue)
                .sum();
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("Sum: " + sum + " (expected: " + (99 * 100 / 2) + ")");
        System.out.println("Time: " + elapsed + "ms");

        if (elapsed > 5000) {
            System.err.println("WARNING: Took too long!");
            System.exit(1);
        }

        System.out.println("Success!");
    }
}
