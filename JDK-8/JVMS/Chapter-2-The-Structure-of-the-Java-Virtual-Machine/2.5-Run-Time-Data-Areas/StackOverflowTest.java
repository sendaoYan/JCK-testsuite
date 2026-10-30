/*
 * @test
 * @summary JVMS 2.5.2 (Java SE 8): each thread has a private Java Virtual Machine
 *          stack; when a computation exceeds the permitted depth, the JVM throws
 *          StackOverflowError rather than corrupting other data areas.
 * @run main StackOverflowTest
 */

public class StackOverflowTest {
    static int depth = 0;

    static void recurse() {
        depth++;
        recurse();
    }

    public static void main(String[] args) {
        boolean caught = false;
        try {
            recurse();
        } catch (StackOverflowError expected) {
            caught = true;
        }
        if (!caught) throw new AssertionError("StackOverflowError expected");
        if (depth < 100) throw new AssertionError("expected meaningful depth, got " + depth);
        System.out.println("PASS");
    }
}
