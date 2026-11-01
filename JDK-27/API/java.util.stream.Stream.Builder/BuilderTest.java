/*
 * @test
 * @summary java.util.stream.Stream.Builder API conformance: add/accept/build.
 * @run main BuilderTest
 */

import java.util.stream.Stream;

public class BuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBuilder() {
        Stream.Builder<Integer> b = Stream.builder();
        b.add(1);
        b.accept(2);
        check(b.build().count() == 2, "build yields added elements");
        Stream.Builder<Integer> b2 = Stream.builder();
        b2.add(1);
        b2.accept(2);
        check(b2.build().mapToInt(Integer::intValue).sum() == 3, "elements sum correctly");
    }

    public static void main(String[] args) {
        testBuilder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
