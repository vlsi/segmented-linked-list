public class ClassLoadTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Loading SpliteratorTest class...");
        Class<?> clazz = Class.forName("com.github.segmentedlist.SpliteratorTest");
        System.out.println("Class loaded: " + clazz.getName());
        System.out.println("Number of methods: " + clazz.getDeclaredMethods().length);
        System.out.println("Test completed successfully!");
    }
}
