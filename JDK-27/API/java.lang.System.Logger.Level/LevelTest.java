/*
 * @test
 * @summary java.lang.System.Logger.Level conformance: white-box structure + black-box behavior.
 * @run main LevelTest
 */

import java.lang.System;

public class LevelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() {
        check(System.Logger.Level.class.isEnum(), "Level is an enum");
        check(Enum.class.isAssignableFrom(System.Logger.Level.class), "Level extends Enum");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testOrdering() {
        check("ALL".equals(System.Logger.Level.ALL.getName()), "ALL name");
        check(System.Logger.Level.ALL.getSeverity() < System.Logger.Level.TRACE.getSeverity(), "ALL<TRACE");
        check(System.Logger.Level.TRACE.getSeverity() < System.Logger.Level.DEBUG.getSeverity(), "TRACE<DEBUG");
        check(System.Logger.Level.DEBUG.getSeverity() < System.Logger.Level.INFO.getSeverity(), "DEBUG<INFO");
        check(System.Logger.Level.INFO.getSeverity() < System.Logger.Level.WARNING.getSeverity(), "INFO<WARNING");
        check(System.Logger.Level.WARNING.getSeverity() < System.Logger.Level.ERROR.getSeverity(), "WARNING<ERROR");
        check(System.Logger.Level.ERROR.getSeverity() < System.Logger.Level.OFF.getSeverity(), "ERROR<OFF");
        check(System.Logger.Level.values().length == 7, "7 levels");
        check(System.Logger.Level.valueOf("INFO") == System.Logger.Level.INFO, "valueOf");
        check("WARNING".equals(System.Logger.Level.WARNING.getName()), "WARNING name");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testOrdering();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
