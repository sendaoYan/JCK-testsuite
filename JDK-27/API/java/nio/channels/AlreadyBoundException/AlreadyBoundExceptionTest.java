/*
 * @test
 * @summary java.nio.channels.AlreadyBoundException conformance: white-box structure + black-box behavior.
 * @run main AlreadyBoundExceptionTest
 */

import java.nio.channels.AlreadyBoundException;

public class AlreadyBoundExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(IllegalStateException.class.isAssignableFrom(AlreadyBoundException.class), "hierarchy: extends IllegalStateException");
        check(new AlreadyBoundException() != null, "no-arg constructor");
        check(new AlreadyBoundException().getMessage() == null, "no-arg message is null");
        check(new AlreadyBoundException().getCause() == null, "no cause by default");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // binding a bound server socket channel twice
        try (java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open()) {
            ssc.bind(null);
            try {
                ssc.bind(null);
                check(false, "second bind should throw");
            } catch (AlreadyBoundException e) {
                passed++;
                check(e instanceof IllegalStateException, "is a IllegalStateException");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
