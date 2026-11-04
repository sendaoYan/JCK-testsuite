/*
 * @test
 * @summary java.util.stream.IntStream.Builder conformance: white-box structure + black-box behavior.
 * @run main IntStreamBuilderTest
 */

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class IntStreamBuilderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void expectThrows(Class<? extends Throwable> ex, Runnable r, String msg) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.stream.IntStream$Builder");
        check(c.isInterface(), "Builder is an interface");
        check(IntConsumer.class.isAssignableFrom(c), "Builder extends IntConsumer");
        check(Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("accept") && m.getParameterCount() == 1),
                "accept(int) declared");
        check(Arrays.stream(c.getDeclaredMethods()).anyMatch(m -> m.getName().equals("add")),
                "add(int) declared");
        check(Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("build") && m.getParameterCount() == 0),
                "build() declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        IntStream.Builder b = IntStream.builder();
        b.add(1);
        b.accept(2);
        b.add(3);
        check(Arrays.equals(b.build().toArray(), new int[]{1, 2, 3}),
              "build yields added elements in order");

        IntStream.Builder b2 = IntStream.builder();
        b2.add(1);
        b2.build();
        expectThrows(IllegalStateException.class, b2::build, "second build() -> IllegalStateException");

        check(IntStream.builder().build().count() == 0, "empty builder -> empty stream");

        IntStream.Builder b3 = IntStream.builder();
        check(b3.add(1) == b3, "add() returns this");
        check(b3.add(2).add(3).build().sum() == 6, "chained add().add()");

        IntStream.Builder b4 = IntStream.builder();
        IntStream.of(5, 6, 7).forEach(b4::accept);
        check(Arrays.equals(b4.build().toArray(), new int[]{5, 6, 7}),
              "builder filled via forEach(accept)");
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

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
