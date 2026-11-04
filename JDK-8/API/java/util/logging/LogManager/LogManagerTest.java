/*
 * @test
 * @summary java.util.logging.LogManager conformance: getLogger/addLogger/getLoggerNames.
 * @run main LogManagerTest
 */

import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LogManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLogManager() {
        LogManager lm = LogManager.getLogManager();
        check(lm != null, "getLogManager");
        Logger l = Logger.getLogger("jck.lm.test");
        check(lm.getLogger("jck.lm.test") == l, "getLogger");
        check(!lm.addLogger(l), "addLogger existing returns false");
        check(lm.getLoggerNames() != null, "getLoggerNames");
        check(lm.getLoggingMXBean() != null, "getLoggingMXBean");
    }

    public static void main(String[] args) {
        testLogManager();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
