/*
 * @test
 * @summary java.lang.System.Logger conformance: interface and System.getLogger.
 * @run main LoggerTest
 */
import java.lang.System;

public class LoggerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(System.Logger.class.isInterface(), "is interface");
        System.Logger log = System.getLogger("logger-test");
        check("logger-test".equals(log.getName()), "getName");
        check(log.isLoggable(System.Logger.Level.INFO), "isLoggable INFO");
        log.log(System.Logger.Level.INFO, "msg");
        log.log(System.Logger.Level.INFO, () -> "supplier");
        log.log(System.Logger.Level.INFO, "obj-msg", new Object());
        passed += 3;
        System.out.println("PASS (" + passed + " assertions)");
    }
}
