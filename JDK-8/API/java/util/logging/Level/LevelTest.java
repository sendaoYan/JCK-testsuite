/*
 * @test
 * @summary java.util.logging.Level conformance: constant int values, getName, parse.
 * @run main LevelTest
 */

import java.util.logging.Level;

public class LevelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLevel() {
        check(Level.OFF.intValue() == Integer.MAX_VALUE, "OFF");
        check(Level.SEVERE.intValue() == 1000, "SEVERE");
        check(Level.WARNING.intValue() == 900, "WARNING");
        check(Level.INFO.intValue() == 800, "INFO");
        check(Level.CONFIG.intValue() == 700, "CONFIG");
        check(Level.FINE.intValue() == 500, "FINE");
        check(Level.FINER.intValue() == 400, "FINER");
        check(Level.FINEST.intValue() == 300, "FINEST");
        check(Level.ALL.intValue() == Integer.MIN_VALUE, "ALL");
        check("WARNING".equals(Level.WARNING.getName()), "getName");
        check("WARNING".equals(Level.WARNING.toString()), "toString");
        check(Level.parse("INFO") == Level.INFO, "parse name");
        check(Level.parse("800") == Level.INFO, "parse int");
    }

    public static void main(String[] args) {
        testLevel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
