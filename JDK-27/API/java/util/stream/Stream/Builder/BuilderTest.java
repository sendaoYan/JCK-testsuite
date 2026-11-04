/*
 * @test
 * @summary java.util.stream.Stream.Builder conformance: white-box structure + black-box behavior.
 * @run main BuilderTest
 */

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BuilderTest {
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
        Class<?> c = Class.forName("java.util.stream.Stream$Builder");
        check(c.isInterface(), "Builder is an interface");
        check(Consumer.class.isAssignableFrom(c), "Builder extends Consumer");
        check(Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("accept") && m.getParameterCount() == 1),
                "accept(T) declared");
        check(Arrays.stream(c.getDeclaredMethods()).anyMatch(m -> m.getName().equals("add")),
                "add(T) declared");
        check(Arrays.stream(c.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("build") && m.getParameterCount() == 0),
                "build() declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Stream.Builder<Integer> b = Stream.builder();
        b.add(1);
        b.accept(2);
        b.add(3);
        check(b.build().collect(Collectors.toList()).equals(List.of(1, 2, 3)),
              "build yields added elements in order");

        Stream.Builder<Integer> b2 = Stream.builder();
        b2.add(1);
        b2.build();
        expectThrows(IllegalStateException.class, b2::build, "second build() -> IllegalStateException");

        check(Stream.builder().build().count() == 0, "empty builder -> empty stream");

        Stream.Builder<String> b3 = Stream.builder();
        check(b3.add("a") == b3, "add() returns this");
        check(b3.add("b").add("c").build().collect(Collectors.joining()).equals("abc"),
              "chained add().add()");

        Stream.Builder<Integer> b4 = Stream.builder();
        Stream.of(5, 6, 7).forEach(b4::accept);
        check(b4.build().mapToInt(Integer::intValue).sum() == 18, "builder filled via forEach(accept)");
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

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
