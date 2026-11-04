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


    // ===================== 契约深化：状态机（build 后 add ISE）/accept/空构建 =====================
    static void testContracts() {
        LongStream.Builder b = LongStream.builder();
        check(b.add(1) == b, "add returns this");
        b.accept(2);
        check(java.util.Arrays.equals(b.build().toArray(), new long[] {1, 2}), "add+accept mix");
        try { b.add(3); check(false, "build-then-add ISE"); }
        catch (IllegalStateException e) { check(true, "build-then-add ISE"); }
        try { b.build(); check(false, "double build ISE"); }
        catch (IllegalStateException e) { check(true, "double build ISE"); }
        check(LongStream.builder().build().count() == 0, "empty builder");
    }

    public static void main(String[] args) {
        testContracts();
        testBuilder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
