/*
 * @test
 * @summary java.util.function.ObjDoubleConsumer API conformance: accept.
 * @run main ObjDoubleConsumerTest
 */

import java.util.function.ObjDoubleConsumer;

public class ObjDoubleConsumerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testObjDoubleConsumer() {
        final double[] captured = new double[1];
        ObjDoubleConsumer<String> c = (s, d) -> captured[0] = s.length() + d;
        c.accept("ab", 3.5);
        check(captured[0] == 5.5, "accept");
    }

    public static void main(String[] args) {
        testObjDoubleConsumer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
