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


    // ===================== 契约深化：状态机（build 后 add ISE）/accept 链式/空构建 =====================
    static void testContracts() {
        Stream.Builder<Integer> b = Stream.builder();
        check(b.add(1) == b, "add returns this");
        b.accept(2);
        check(b.build().collect(java.util.stream.Collectors.toList()).equals(java.util.Arrays.asList(1, 2)), "add+accept mix");
        try { b.add(3); check(false, "build-then-add ISE"); }
        catch (IllegalStateException e) { check(true, "build-then-add ISE"); }
        try { b.build(); check(false, "double build ISE"); }
        catch (IllegalStateException e) { check(true, "double build ISE"); }
        check(Stream.builder().build().count() == 0, "empty builder");
    }

    public static void main(String[] args) {
        testContracts();
        testBuilder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
