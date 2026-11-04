/*
 * @test
 * @summary java.util.stream.Gatherers conformance: white-box structure + black-box behavior.
 * @run main GatherersTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

public class GatherersTest {
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
        Class<?> c = Class.forName("java.util.stream.Gatherers");
        check(!c.isInterface() && !c.isEnum(), "Gatherers is a class");
        check(Modifier.isFinal(c.getModifiers()), "Gatherers is final");
        String[] names = Arrays.stream(c.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .distinct()
                .sorted()
                .toArray(String[]::new);
        check(Arrays.equals(names, new String[]{"fold", "mapConcurrent", "scan", "windowFixed", "windowSliding"}),
              "exactly the five public factory methods");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // windowFixed
        check(Stream.of(1, 2, 3, 4, 5).gather(Gatherers.windowFixed(2)).toList()
                .equals(List.of(List.of(1, 2), List.of(3, 4), List.of(5))),
                "windowFixed(2) with partial last window");
        check(Stream.of(1, 2, 3, 4).gather(Gatherers.windowFixed(2)).toList()
                .equals(List.of(List.of(1, 2), List.of(3, 4))),
                "windowFixed(2) exact multiple has no partial window");
        check(Stream.of(1, 2, 3).gather(Gatherers.windowFixed(1)).toList()
                .equals(List.of(List.of(1), List.of(2), List.of(3))),
                "windowFixed(1) singletons");

        // windowSliding
        check(Stream.of(1, 2, 3, 4, 5).gather(Gatherers.windowSliding(2)).toList()
                .equals(List.of(List.of(1, 2), List.of(2, 3), List.of(3, 4), List.of(4, 5))),
                "windowSliding(2) overlapping windows");
        check(Stream.of(1, 2, 3).gather(Gatherers.windowSliding(1)).toList()
                .equals(List.of(List.of(1), List.of(2), List.of(3))),
                "windowSliding(1) singletons");

        // fold
        check(Stream.of(1, 2, 3, 4, 5).gather(Gatherers.fold(() -> 0, Integer::sum)).toList()
                .equals(List.of(15)), "fold reduces to a single element");
        check(Stream.of("a", "b", "c").gather(Gatherers.fold(() -> "", (a, b) -> a + b)).toList()
                .equals(List.of("abc")), "fold string concatenation");

        // scan
        check(Stream.of(1, 2, 3, 4, 5).gather(Gatherers.scan(() -> 0, Integer::sum)).toList()
                .equals(List.of(1, 3, 6, 10, 15)), "scan emits running totals");

        // mapConcurrent
        check(Stream.of(1, 2, 3, 4, 5)
                .gather(Gatherers.mapConcurrent(4, x -> x * 2))
                .sorted()
                .toList()
                .equals(List.of(2, 4, 6, 8, 10)), "mapConcurrent applies the mapping");

        // invalid sizes
        expectThrows(IllegalArgumentException.class, () -> Gatherers.windowFixed(0),
                "windowFixed(0) -> IllegalArgumentException");
        expectThrows(IllegalArgumentException.class, () -> Gatherers.windowSliding(0),
                "windowSliding(0) -> IllegalArgumentException");
        expectThrows(IllegalArgumentException.class, () -> Gatherers.windowSliding(-1),
                "windowSliding(-1) -> IllegalArgumentException");
        expectThrows(IllegalArgumentException.class, () -> Gatherers.mapConcurrent(0, x -> x),
                "mapConcurrent(0) -> IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
