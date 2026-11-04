/*
 * @test
 * @summary java.util.function.BiConsumer API conformance: accept and andThen composition.
 * @run main BiConsumerTest
 */

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class BiConsumerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBiConsumer() {
        Map<String, Integer> m = new HashMap<>();
        BiConsumer<String, Integer> put = m::put;
        put.accept("a", 1);
        check(m.get("a") == 1, "accept");
        BiConsumer<String, Integer> putTenMore = (k, v) -> m.put(k, v + 10);
        put.andThen(putTenMore).accept("b", 2);
        check(m.get("b") == 12, "andThen");
    }

    public static void main(String[] args) {
        testBiConsumer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
