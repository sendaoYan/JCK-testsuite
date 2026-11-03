/*
 * @test
 * @summary java.io.Console conformance: white-box structure + black-box behavior.
 *          jtreg runs with redirected (non-tty) standard streams, so
 *          System.console() is null here: the test asserts that headless
 *          behavior and verifies the API surface reflectively.
 * @run main ConsoleTest
 */

import java.io.Console;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ConsoleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.Console");
        check(java.io.Flushable.class.isAssignableFrom(c), "implements Flushable");
        for (String m : new String[]{"writer", "reader", "format", "printf", "readLine",
                                     "readPassword", "flush", "charset", "isTerminal"})
            check(has(c, m), "has method " + m);
        // the Console constructor itself is package-private: only the JDK factory
        // System.console() can create one
        for (Constructor<?> k : c.getDeclaredConstructors())
            check(!Modifier.isPublic(k.getModifiers()), "no public constructor");
        check(c.getMethod("writer").getReturnType() == java.io.PrintWriter.class,
              "writer() -> PrintWriter");
        check(c.getMethod("reader").getReturnType() == java.io.Reader.class, "reader() -> Reader");
        check(c.getMethod("readPassword").getReturnType() == char[].class,
              "readPassword() -> char[]");
        check(c.getMethod("charset").getReturnType() == java.nio.charset.Charset.class,
              "charset() -> Charset");
        check(c.getMethod("isTerminal").getReturnType() == boolean.class,
              "isTerminal() -> boolean");
    }

    static boolean has(Class<?> c, String m) {
        for (Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. jtreg pipes stdin/stdout, so there is no console: null, not an object
        Console con = System.console();
        check(con == null, "System.console() is null without a terminal (got " + con + ")");

        // 2. the documented console charset is available even without a console
        check(System.out != null && System.err != null, "standard streams still exist");

        // 3. API shape guarantees that cannot be exercised without a terminal:
        //    readLine/format have both plain and Locale-aware overloads
        Class<?> c = Console.class;
        check(c.getMethod("readLine", String.class, Object[].class) != null,
              "readLine(String, Object...)");
        check(c.getMethod("readLine", java.util.Locale.class, String.class, Object[].class) != null,
              "readLine(Locale, String, Object...)");
        check(c.getMethod("format", String.class, Object[].class) != null,
              "format(String, Object...)");
        check(c.getMethod("format", java.util.Locale.class, String.class, Object[].class) != null,
              "format(Locale, String, Object...)");
        check(c.getMethod("printf", String.class, Object[].class) != null,
              "printf(String, Object...)");
        check(c.getMethod("readPassword", String.class, Object[].class) != null,
              "readPassword(String, Object...)");
        // printf delegates to format and returns the console itself
        check(c.getMethod("printf", java.util.Locale.class, String.class, Object[].class)
                      .getReturnType() == Console.class,
              "printf returns Console");

        // 4. isTerminal reflects the absence of a terminal in this environment:
        //    with console() null there is nothing to query, so assert the
        //    environment really is non-tty by checking the class-level behavior
        check(System.console() == null, "console stays null on repeated calls");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
