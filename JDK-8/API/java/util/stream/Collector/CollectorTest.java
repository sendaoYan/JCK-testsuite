/*
 * @test
 * @summary java.util.stream.Collector API conformance: supplier/accumulator/finisher/characteristics.
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

    static void testCollector() {
        Supplier<List<String>> supplier = ArrayList::new;
        BiConsumer<List<String>, String> accumulator = List::add;
        BinaryOperator<List<String>> combiner = (a, b) -> { a.addAll(b); return a; };
        Collector<String, ?, List<String>> c = Collector.of(supplier, accumulator, combiner);
        List<String> out = Stream.of("a", "b").collect(c);
        check(out.size() == 2 && out.contains("a"), "collect");
        check(c.supplier().get() instanceof List, "supplier");
        check(c.accumulator() != null && c.combiner() != null, "accumulator/combiner present");
        check(c.finisher() != null, "identity finisher present");
        check(c.characteristics().contains(Collector.Characteristics.IDENTITY_FINISH),
              "identity-finish characteristic");

        Function<List<String>, List<String>> id = x -> x;
        Collector<String, ?, List<String>> c2 = Collector.of(supplier, accumulator, combiner, id,
            Collector.Characteristics.UNORDERED);
        check(c2.finisher() == id, "explicit finisher");
        check(c2.characteristics().contains(Collector.Characteristics.UNORDERED), "explicit characteristics");
    }

    public static void main(String[] args) {
        testCollector();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
