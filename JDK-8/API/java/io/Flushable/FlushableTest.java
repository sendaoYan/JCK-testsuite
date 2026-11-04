/*
 * @test
 * @summary java.io.Flushable API conformance: flush().
 * @run main FlushableTest
 */

import java.io.Flushable;

public class FlushableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFlushable() throws Exception {
        Flushable f = () -> {};
        f.flush();
        passed++; // flush() succeeded
    }

    public static void main(String[] args) throws Exception {
        testFlushable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
