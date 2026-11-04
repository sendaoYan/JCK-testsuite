/*
 * @test
 * @summary java.util.stream.DoubleStream.Builder conformance: white-box structure + black-box behavior.
 * @run main DoubleStreamBuilderTest
 */

import java.util.Arrays;
import java.util.function.DoubleConsumer;
import java.util.stream.DoubleStream;

public class DoubleStreamBuilderTest {
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
        Class<?> c = Class.forName("java.util.stream.DoubleStream$Builder");
        check(c.isInterface(), "Builder is an interface");
        check(DoubleConsumer.class.isAssignableFrom(c), "Builder extends DoubleConsumer");
        boolean accept = Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("accept") && m.getParameterCount() == 1);
        check(accept, "accept(double) declared");
        boolean add = Arrays.stream(c.getDeclaredMethods()).anyMatch(m -> m.getName().equals("add"));
        check(add, "add(double) declared");
        boolean build = Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("build") && m.getParameterCount() == 0);
        check(build, "build() declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DoubleStream.Builder b = DoubleStream.builder();
        b.add(1.0);
        b.accept(2.0);
        b.add(3.0);
        check(Arrays.equals(b.build().toArray(), new double[]{1.0, 2.0, 3.0}),
              "build yields added elements in order");

        DoubleStream.Builder b2 = DoubleStream.builder();
        b2.add(1.0);
        b2.build();
        expectThrows(IllegalStateException.class, b2::build, "second build() -> IllegalStateException");

        check(DoubleStream.builder().build().count() == 0, "empty builder -> empty stream");

        DoubleStream.Builder b3 = DoubleStream.builder();
        check(b3.add(1.0) == b3, "add() returns this");
        check(b3.add(2.0).add(3.0).build().sum() == 6.0, "chained add().add()");

        DoubleStream.Builder b4 = DoubleStream.builder();
        DoubleStream.of(5.0, 6.0, 7.0).forEach(b4::accept);
        check(Arrays.equals(b4.build().toArray(), new double[]{5.0, 6.0, 7.0}),
              "builder filled via forEach(accept)");
    }


    // ===================== 契约深化：状态机（build 后 add ISE）/accept/空构建 =====================
    static void testContracts() {
        DoubleStream.Builder b = DoubleStream.builder();
        check(b.add(1) == b, "add returns this");
        b.accept(2);
        check(java.util.Arrays.equals(b.build().toArray(), new double[] {1, 2}), "add+accept mix");
        try { b.add(3); check(false, "build-then-add ISE"); }
        catch (IllegalStateException e) { check(true, "build-then-add ISE"); }
        try { b.build(); check(false, "double build ISE"); }
        catch (IllegalStateException e) { check(true, "double build ISE"); }
        check(DoubleStream.builder().build().count() == 0, "empty builder");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
