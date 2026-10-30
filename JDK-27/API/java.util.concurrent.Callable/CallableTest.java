/*
 * @test
 * @summary java.util.concurrent.Callable API conformance: call returns a value.
 * @run main CallableTest
 */

import java.util.concurrent.Callable;

public class CallableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCallable() throws Exception {
        Callable<Integer> c = new Callable<Integer>() {
            public Integer call() {
                return 7;
            }
        };
        check(c.call() == 7, "call returns the computed value");
    }

    public static void main(String[] args) throws Exception {
        testCallable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
