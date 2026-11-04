/*
 * @test
 * @summary java.util.stream.Collector conformance: white-box structure + black-box behavior.
 * @run main CollectorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class CollectorTest {
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
        Class<?> c = Class.forName("java.util.stream.Collector");
        check(c.isInterface(), "Collector is an interface");
        check(Modifier.isPublic(c.getModifiers()), "Collector is public");
        String[] names = {"supplier", "accumulator", "combiner", "finisher", "characteristics"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares " + n + "()");
        }
        long ofCount = 0;
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals("of")) ofCount++;
        check(ofCount == 2, "two of() overloads");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Supplier<List<String>> supplier = ArrayList::new;
        BiConsumer<List<String>, String> accumulator = List::add;
        BinaryOperator<List<String>> combiner = (a, b) -> { a.addAll(b); return a; };

        // 3-arg of -> identity finisher, drive via Stream.collect
        Collector<String, ?, List<String>> c = Collector.of(supplier, accumulator, combiner);
        List<String> out = Stream.of("a", "b", "c").collect(c);
        check(out.equals(List.of("a", "b", "c")), "3-arg of() collects in order");
        check(c.supplier().get() instanceof List, "supplier() yields a list");
        check(c.accumulator() != null && c.combiner() != null, "accumulator/combiner present");
        check(c.finisher() != null, "identity finisher present");
        Set<Collector.Characteristics> chars = c.characteristics();
        check(chars.contains(Collector.Characteristics.IDENTITY_FINISH), "identity-finish characteristic");

        // 4-arg of with a finisher: join into a comma-separated string
        Function<List<String>, String> joiner = list -> {
            StringJoiner sj = new StringJoiner(",");
            list.forEach(sj::add);
            return sj.toString();
        };
        Collector<String, ?, String> c2 = Collector.of(supplier, accumulator, combiner, joiner,
                Collector.Characteristics.UNORDERED);
        String joined = Stream.of("x", "y", "z").collect(c2);
        check(joined.equals("x,y,z"), "4-arg of() applies finisher");
        check(c2.finisher() == joiner, "explicit finisher identity");
        check(c2.characteristics().contains(Collector.Characteristics.UNORDERED), "explicit characteristics");
        check(!c2.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH),
              "no identity-finish when a finisher is supplied");

        // drive a custom collector through filter/map/collect
        List<Integer> even = Stream.of(1, 2, 3, 4, 5, 6)
                .filter(x -> x % 2 == 0)
                .map(x -> x * 10)
                .collect(Collector.of(ArrayList::new, List::add, (a, b) -> { a.addAll(b); return a; }));
        check(even.equals(List.of(20, 40, 60)), "collector driven by full chain");

        // null arguments rejected
        expectThrows(NullPointerException.class, () -> Collector.of(null, accumulator, combiner),
                "null supplier -> NPE");
        expectThrows(NullPointerException.class, () -> Collector.of(supplier, null, combiner),
                "null accumulator -> NPE");
        expectThrows(NullPointerException.class,
                () -> Collector.of(supplier, accumulator, combiner, (Function<List<String>, String>) null),
                "null finisher -> NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
