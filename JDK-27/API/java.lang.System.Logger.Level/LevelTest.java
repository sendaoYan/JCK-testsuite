/*
 * @test
 * @summary java.lang.System.Logger.Level conformance: enum getName/getSeverity ordering.
 * @run main LevelTest
 */
import java.lang.System;

public class LevelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(System.Logger.Level.class.isEnum(), "is enum");
        check("ALL".equals(System.Logger.Level.ALL.getName()), "ALL name");
        check(System.Logger.Level.ALL.getSeverity() < System.Logger.Level.TRACE.getSeverity(), "ALL<TRACE");
        check(System.Logger.Level.TRACE.getSeverity() < System.Logger.Level.DEBUG.getSeverity(), "TRACE<DEBUG");
        check(System.Logger.Level.DEBUG.getSeverity() < System.Logger.Level.INFO.getSeverity(), "DEBUG<INFO");
        check(System.Logger.Level.INFO.getSeverity() < System.Logger.Level.WARNING.getSeverity(), "INFO<WARNING");
        check(System.Logger.Level.WARNING.getSeverity() < System.Logger.Level.ERROR.getSeverity(), "WARNING<ERROR");
        check(System.Logger.Level.ERROR.getSeverity() < System.Logger.Level.OFF.getSeverity(), "ERROR<OFF");
        check(System.Logger.Level.values().length == 7, "7 levels");
        check(System.Logger.Level.valueOf("INFO") == System.Logger.Level.INFO, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
