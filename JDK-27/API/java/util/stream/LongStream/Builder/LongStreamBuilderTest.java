/*
 * @test
 * @summary java.util.stream.LongStream.Builder conformance: white-box structure + black-box behavior.
 * @run main LongStreamBuilderTest
 */

import java.util.Arrays;
import java.util.function.LongConsumer;
import java.util.stream.LongStream;

public class LongStreamBuilderTest {
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
        Class<?> c = Class.forName("java.util.stream.LongStream$Builder");
        check(c.isInterface(), "Builder is an interface");
        check(LongConsumer.class.isAssignableFrom(c), "Builder extends LongConsumer");
        check(Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("accept") && m.getParameterCount() == 1),
                "accept(long) declared");
        check(Arrays.stream(c.getDeclaredMethods()).anyMatch(m -> m.getName().equals("add")),
                "add(long) declared");
        check(Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("build") && m.getParameterCount() == 0),
                "build() declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        LongStream.Builder b = LongStream.builder();
        b.add(1L);
        b.accept(2L);
        b.add(3L);
        check(Arrays.equals(b.build().toArray(), new long[]{1L, 2L, 3L}),
              "build yields added elements in order");

        LongStream.Builder b2 = LongStream.builder();
        b2.add(1L);
        b2.build();
        expectThrows(IllegalStateException.class, b2::build, "second build() -> IllegalStateException");

        check(LongStream.builder().build().count() == 0, "empty builder -> empty stream");

        LongStream.Builder b3 = LongStream.builder();
        check(b3.add(1L) == b3, "add() returns this");
        check(b3.add(2L).add(3L).build().sum() == 6L, "chained add().add()");

        LongStream.Builder b4 = LongStream.builder();
        LongStream.of(5L, 6L, 7L).forEach(b4::accept);
        check(Arrays.equals(b4.build().toArray(), new long[]{5L, 6L, 7L}),
              "builder filled via forEach(accept)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
