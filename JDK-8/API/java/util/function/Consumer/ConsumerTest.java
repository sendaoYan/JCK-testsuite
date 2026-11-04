/*
 * @test
 * @summary java.util.function.Consumer API conformance: accept and andThen composition.
 * @run main ConsumerTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ConsumerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConsumer() {
        List<String> seen = new ArrayList<>();
        Consumer<String> c = seen::add;
        c.accept("a");
        check(seen.equals(Arrays.asList("a")), "accept");
        Consumer<String> c2 = s -> seen.add(s + "!");
        c.andThen(c2).accept("b");
        check(seen.equals(Arrays.asList("a", "b", "b!")), "andThen");
    }

    public static void main(String[] args) {
        testConsumer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
