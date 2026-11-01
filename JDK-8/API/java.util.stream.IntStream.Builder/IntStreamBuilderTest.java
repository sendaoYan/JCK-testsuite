/*
 * @test
 * @summary java.util.stream.IntStream.Builder API conformance: add/accept/build.
 * @run main IntStreamBuilderTest
 */

import java.util.stream.IntStream;

public class IntStreamBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBuilder() {
        IntStream.Builder b = IntStream.builder();
        b.add(1);
        b.accept(2);
        check(b.build().sum() == 3, "build yields added elements");
    }

    public static void main(String[] args) {
        testBuilder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
