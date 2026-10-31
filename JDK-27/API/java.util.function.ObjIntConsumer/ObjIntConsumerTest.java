/*
 * @test
 * @summary java.util.function.ObjIntConsumer API conformance: accept.
 * @run main ObjIntConsumerTest
 */

import java.util.function.ObjIntConsumer;

public class ObjIntConsumerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testObjIntConsumer() {
        final int[] captured = new int[1];
        ObjIntConsumer<String> c = (s, i) -> captured[0] = s.length() + i;
        c.accept("ab", 3);
        check(captured[0] == 5, "accept");
    }

    public static void main(String[] args) {
        testObjIntConsumer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
