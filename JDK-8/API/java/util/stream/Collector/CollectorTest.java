/*
 * @test
 * @summary java.util.stream.Collector contract: of() factory forms, supplier/accumulator/
 *          combiner/finisher, characteristics, null->NPE for each argument.
 * @run main CollectorTest
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static Supplier<List<String>> supplier() { return ArrayList::new; }
    static BiConsumer<List<String>, String> accumulator() { return List::add; }
    static BinaryOperator<List<String>> combiner() { return (a, b) -> { a.addAll(b); return a; }; }

    static void testCollectorOf() {
        Collector<String, ?, List<String>> c = Collector.of(supplier(), accumulator(), combiner());
        List<String> out = Stream.of("a", "b").collect(c);
        check(out.size() == 2 && out.contains("a"), "collect");
        check(c.supplier().get() instanceof List, "supplier");
        check(c.accumulator() != null && c.combiner() != null, "accumulator/combiner");
        check(c.finisher() != null, "identity finisher");
        check(c.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH), "identity-finish char");

        Function<List<String>, Set<String>> finisher = s -> new java.util.HashSet<>(s);
        Collector<String, ?, Set<String>> c2 = Collector.of(supplier(), accumulator(), combiner(), finisher,
                Collector.Characteristics.UNORDERED, Collector.Characteristics.CONCURRENT);
        Set<String> set = Stream.of("a", "b", "a").collect(c2);
        check(set.size() == 2, "collect with finisher");
        check(c2.finisher() == finisher, "explicit finisher");
        check(c2.characteristics().contains(Collector.Characteristics.UNORDERED), "UNORDERED char");
        check(c2.characteristics().contains(Collector.Characteristics.CONCURRENT), "CONCURRENT char");
        check(c2.characteristics().size() == 2, "characteristics size");
    }

    static void testNullArguments() {
        checkThrows(NullPointerException.class, () -> Collector.of(null, accumulator(), combiner()), "of null supplier NPE");
        checkThrows(NullPointerException.class, () -> Collector.of(supplier(), null, combiner()), "of null accumulator NPE");
        checkThrows(NullPointerException.class, () -> Collector.of(supplier(), accumulator(), null), "of null combiner NPE");
        checkThrows(NullPointerException.class,
                () -> Collector.of(supplier(), accumulator(), combiner(),
                        (Function<List<String>, Set<String>>) null, Collector.Characteristics.UNORDERED),
                "of null finisher NPE");
    }

    static void testCharacteristicsEnum() {
        for (Collector.Characteristics ch : Collector.Characteristics.values()) {
            check(ch != null, "characteristic " + ch);
        }
        check(Collector.Characteristics.CONCURRENT != Collector.Characteristics.UNORDERED, "distinct enum values");
        check(Collector.Characteristics.valueOf("IDENTITY_FINISH") == Collector.Characteristics.IDENTITY_FINISH, "valueOf");
    }

    public static void main(String[] args) {
        testCollectorOf();
        testNullArguments();
        testCharacteristicsEnum();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
