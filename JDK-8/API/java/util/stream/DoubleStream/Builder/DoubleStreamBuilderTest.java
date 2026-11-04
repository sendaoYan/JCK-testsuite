/*
 * @test
 * @summary java.util.stream.DoubleStream.Builder API conformance: add/accept/build.
 * @run main DoubleStreamBuilderTest
 */

import java.util.stream.DoubleStream;

public class DoubleStreamBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBuilder() {
        DoubleStream.Builder b = DoubleStream.builder();
        b.add(1.0);
        b.accept(2.0);
        check(b.build().sum() == 3.0, "build yields added elements");
    }

    public static void main(String[] args) {
        testBuilder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
