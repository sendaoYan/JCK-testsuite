/*
 * @test
 * @summary java.util.function.ObjLongConsumer API conformance: accept.
 * @run main ObjLongConsumerTest
 */

import java.util.function.ObjLongConsumer;

public class ObjLongConsumerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testObjLongConsumer() {
        final long[] captured = new long[1];
        ObjLongConsumer<String> c = (s, l) -> captured[0] = s.length() + l;
        c.accept("ab", 3L);
        check(captured[0] == 5L, "accept");
    }

    public static void main(String[] args) {
        testObjLongConsumer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
