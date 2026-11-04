/*
 * @test
 * @summary java.util.stream.LongStream.Builder API conformance: add/accept/build.
 * @run main LongStreamBuilderTest
 */

import java.util.stream.LongStream;

public class LongStreamBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBuilder() {
        LongStream.Builder b = LongStream.builder();
        b.add(1L);
        b.accept(2L);
        check(b.build().sum() == 3L, "build yields added elements");
    }

    public static void main(String[] args) {
        testBuilder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
