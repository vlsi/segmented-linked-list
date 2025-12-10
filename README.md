# SegmentedLinkedList

A production-grade Java `List` implementation that uses a segmented array approach to combine the benefits of linked lists and arrays. Each node contains an array of 16 elements, reducing memory overhead and improving cache locality compared to traditional `LinkedList`.

## Features

- ✅ Full `java.util.List` interface implementation (Java 21)
- ✅ Complete `java.util.Deque` support
- ✅ Full `ListIterator` with bidirectional traversal
- ✅ `Serializable` with custom serialization
- ✅ `Cloneable` with efficient deep copy
- ✅ Null element support
- ✅ Fail-fast iteration
- ✅ Comprehensive test coverage (Guava testlib)

## Why SegmentedLinkedList?

Traditional `LinkedList` stores one element per node, leading to:
- High memory overhead (24-32 bytes per node for pointers)
- Poor cache locality (nodes scattered in memory)
- Frequent allocations

`SegmentedLinkedList` addresses these issues by:
- Storing 16 elements per segment (reduced pointer overhead)
- Improving cache locality (sequential array access)
- Reducing allocation frequency (bulk element storage)

## Time Complexity

| Operation | Time Complexity |
|-----------|----------------|
| `add(E)` (at end) | O(1) amortized |
| `addFirst(E)` | O(1) amortized |
| `addLast(E)` | O(1) amortized |
| `add(int, E)` | O(n) |
| `get(int)` | O(n/16) |
| `set(int, E)` | O(n/16) |
| `remove(int)` | O(n) |
| `removeFirst()` | O(1) |
| `removeLast()` | O(1) |
| `iterator.next()` | O(1) |
| `size()` | O(1) |

## Usage

```java
import io.github.vlsi.lists.SegmentedLinkedList;

// Create an empty list
SegmentedLinkedList<String> list = new SegmentedLinkedList<>();

// Add elements
list.

        add("first");
list.

        addLast("last");
list.

        addFirst("new-first");

        // Access elements
        String element = list.get(0);
        String first = list.getFirst();
        String last = list.getLast();

// Use as Deque
list.

        push("top");

        String popped = list.pop();

// Iterate
for(
        String item :list){
        System.out.

        println(item);
}

        // Use ListIterator
        ListIterator<String> it = list.listIterator();
while(it.

        hasNext()){
        String item = it.next();
    it.

        set(item.toUpperCase()); // Modify during iteration
        }

        // Clone
        SegmentedLinkedList<String> clone = list.clone();

        // Serialization (implements Serializable)
        ObjectOutputStream oos = new ObjectOutputStream(fileOut);
oos.

        writeObject(list);
```

## Building

```bash
# Compile
mvn clean compile

# Run tests (includes 400+ automated Guava testlib tests)
mvn test

# Create JAR
mvn package

# Run benchmarks
mvn clean package
java -jar target/benchmarks.jar
```

## Testing

The project uses **Guava testlib** to generate comprehensive test suites that validate all `List` contract requirements:

- 400+ automated tests covering all operations
- Edge cases and null handling
- Iterator behavior and concurrent modification
- Sublist operations
- Serialization and cloning tests

Run tests:
```bash
mvn test
```

## Benchmarks

JMH benchmarks compare performance against `LinkedList` and `ArrayList`:

```bash
mvn clean package
java -jar target/benchmarks.jar
```

Benchmark categories:
- **Add operations**: `addFirst`, `addLast`, `add(middle)`
- **Access operations**: `get(index)`, iteration
- **Remove operations**: `removeFirst`, `removeLast`, `remove(middle)`
- **Search operations**: `contains`
- **Memory footprint**: heap consumption comparison

## Project Structure

```
segmented-linked-list/
├── src/
│   ├── main/java/com/github/segmentedlist/
│   │   └── SegmentedLinkedList.java
│   └── test/java/com/github/segmentedlist/
│       ├── SegmentedLinkedListTest.java
│       ├── SerializationTest.java
│       └── SegmentedLinkedListBenchmark.java
├── pom.xml
└── README.md
```

## Requirements

- Java 21 or higher
- Maven 3.6+

## Dependencies

- **Guava testlib** 33.0.0-jre (test)
- **JUnit Jupiter** 5.10.1 (test)
- **JMH** 1.37 (benchmarks)

## Design Details

### Segment Structure

Each segment contains:
- `Object[] elements`: Array of 16 elements
- `int size`: Current number of elements in segment
- `Segment<E> prev`: Previous segment reference
- `Segment<E> next`: Next segment reference

### Memory Efficiency

Compared to `LinkedList`:
- **LinkedList**: 24-32 bytes overhead per element (depending on platform)
- **SegmentedLinkedList**: ~3-4 bytes overhead per element (16 elements share pointers)

For a list of 1000 elements:
- `LinkedList`: ~24-32 KB overhead
- `SegmentedLinkedList`: ~3-4 KB overhead

### Iterator Implementation

The `ListIterator` implementation supports:
- Forward and backward traversal
- `add`, `remove`, `set` during iteration
- Fail-fast concurrent modification detection
- Proper index tracking across segments

## Contributing

Contributions are welcome! Please ensure:
- All tests pass (`mvn test`)
- Code follows Java conventions
- New features include tests
- Javadoc is updated

## License

This project is provided as-is for educational and production use.

## Comparison with Java Collections

| Feature | SegmentedLinkedList | LinkedList | ArrayList |
|---------|-------------------|------------|-----------|
| Add at end | O(1) | O(1) | O(1) amortized |
| Add at start | O(1) | O(1) | O(n) |
| Get by index | O(n/16) | O(n) | O(1) |
| Remove first | O(1) | O(1) | O(n) |
| Memory overhead | Low | High | Low |
| Cache locality | Good | Poor | Excellent |
| Deque operations | ✅ | ✅ | ❌ |

## Performance Expectations

Based on design characteristics:

- **Outperforms LinkedList**: Sequential access, memory usage, cache efficiency
- **Between LinkedList and ArrayList**: Random access performance
- **Best use cases**:
  - Frequent insertions/deletions at both ends
  - Sequential iteration
  - Memory-constrained environments
  - Cache-sensitive applications

## Future Enhancements

Potential optimizations for future versions:
- Dynamic segment sizing based on usage patterns
- Segment pooling for reduced GC pressure
- Indexing hints for frequently accessed positions
- Optimized bulk operations
- Spliterator for parallel stream support

## References

- [Guava testlib documentation](https://github.com/google/guava/wiki/GuavaTestingExplained)
- [JMH documentation](https://openjdk.org/projects/code-tools/jmh/)
- [Java Collections Framework](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/doc-files/coll-overview.html)
