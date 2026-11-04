/*
 * @test
 * @summary java.util.logging.ErrorManager conformance: error() and failure constants.
 * @run main ErrorManagerTest
 */

import java.util.logging.ErrorManager;

public class ErrorManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testErrorManager() {
        ErrorManager em = new ErrorManager();
        em.error("message", new RuntimeException("cause"), ErrorManager.WRITE_FAILURE);
        check(ErrorManager.GENERIC_FAILURE == 0, "GENERIC_FAILURE");
        check(ErrorManager.WRITE_FAILURE == 1, "WRITE_FAILURE");
        check(ErrorManager.FLUSH_FAILURE == 2, "FLUSH_FAILURE");
        check(ErrorManager.CLOSE_FAILURE == 3, "CLOSE_FAILURE");
        check(ErrorManager.OPEN_FAILURE == 4, "OPEN_FAILURE");
        check(ErrorManager.FORMAT_FAILURE == 5, "FORMAT_FAILURE");
    }

    public static void main(String[] args) {
        testErrorManager();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
