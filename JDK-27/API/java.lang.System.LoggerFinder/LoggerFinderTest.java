/*
 * @test
 * @summary java.lang.System.LoggerFinder conformance: abstract class and getLoggerFinder.
 * @run main LoggerFinderTest
 */
import java.lang.System;
import java.lang.reflect.Modifier;

public class LoggerFinderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(Modifier.isAbstract(System.LoggerFinder.class.getModifiers()), "abstract");
        check(System.LoggerFinder.getLoggerFinder() != null, "getLoggerFinder non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
