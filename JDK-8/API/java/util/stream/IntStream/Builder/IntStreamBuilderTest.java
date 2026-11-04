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


    // ===================== 契约深化：状态机（build 后 add ISE）/accept/空构建 =====================
    static void testContracts() {
        IntStream.Builder b = IntStream.builder();
        check(b.add(1) == b, "add returns this");
        b.accept(2);
        check(java.util.Arrays.equals(b.build().toArray(), new int[] {1, 2}), "add+accept mix");
        try { b.add(3); check(false, "build-then-add ISE"); }
        catch (IllegalStateException e) { check(true, "build-then-add ISE"); }
        try { b.build(); check(false, "double build ISE"); }
        catch (IllegalStateException e) { check(true, "double build ISE"); }
        check(IntStream.builder().build().count() == 0, "empty builder");
    }

    public static void main(String[] args) {
        testContracts();
        testBuilder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
