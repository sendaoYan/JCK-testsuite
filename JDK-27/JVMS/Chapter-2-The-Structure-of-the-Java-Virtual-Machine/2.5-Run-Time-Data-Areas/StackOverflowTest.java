/*
 * @test
 * @summary JVMS 2.5.2: when a thread requires a larger stack than permitted, the
 *          Java Virtual Machine throws StackOverflowError.
 * @run main StackOverflowTest
 */

public class StackOverflowTest {
    static void recurse() {
        recurse();
    }

    public static void main(String[] args) {
        try {
            recurse();
            throw new AssertionError("no StackOverflowError thrown");
        } catch (StackOverflowError e) {
            // expected per JVMS 2.5.2
        }

        System.out.println("PASS");
    }
}
