/*
 * @test
 * @summary java.util.logging.LoggingMXBean conformance: logger names/levels.
 * @run main LoggingMXBeanTest
 */

import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.LoggingMXBean;

public class LoggingMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLoggingMXBean() {
        LogManager lm = LogManager.getLogManager();
        LoggingMXBean mx = lm.getLoggingMXBean();
        check(mx != null, "getLoggingMXBean");
        check(mx.getLoggerNames() != null, "getLoggerNames");
        Logger.getLogger("jck.mxbean.test");
        mx.setLoggerLevel("jck.mxbean.test", "INFO");
        check("INFO".equals(mx.getLoggerLevel("jck.mxbean.test")), "get/setLoggerLevel");
        check(mx.getParentLoggerName("jck.mxbean.test") != null, "getParentLoggerName");
    }

    public static void main(String[] args) {
        testLoggingMXBean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
