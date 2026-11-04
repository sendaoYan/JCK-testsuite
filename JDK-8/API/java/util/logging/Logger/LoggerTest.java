/*
 * @test
 * @summary java.util.logging.Logger conformance: getLogger/level/log/isLoggable.
 * @run main LoggerTest
 */

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLogger() {
        Logger logger = Logger.getLogger("jck.test.logger");
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.INFO);
        check(logger.getLevel() == Level.INFO, "get/setLevel");
        check("jck.test.logger".equals(logger.getName()), "getName");
        check(logger.getParent() != null, "getParent");
        check(Logger.getLogger("jck.test.logger") == logger, "getLogger returns same instance");
        check(Logger.getGlobal() != null, "getGlobal");
        logger.log(Level.INFO, "message");
        logger.info("info");
        logger.warning("warning");
        logger.fine("fine");
        check(logger.isLoggable(Level.INFO), "isLoggable INFO");
        check(!logger.isLoggable(Level.FINE), "isLoggable FINE false");
        check(logger.getResourceBundleName() == null, "no resource bundle");
        check(logger.getHandlers().length == 0, "no handlers");
    }

    public static void main(String[] args) {
        testLogger();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
