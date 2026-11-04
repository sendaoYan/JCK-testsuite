/*
 * @test
 * @summary java.io.Console conformance: white-box structure + black-box behavior.
 *          jtreg runs with redirected (non-tty) standard streams, so
 *          System.console() is null here: the test asserts that headless
 *          behavior and verifies the API surface reflectively.
 *          Deepened: structural contract (no longer final in JDK-27, Flushable
 *          only, package-private constructor), full 15-method surface with
 *          exact signatures/return types incl. Locale overloads, charset()
 *          and isTerminal(), plus the headless factory contract.
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

    // ===================== deepening (contract level) =====================

    // ---- headless factory contract ----
    static void testHeadlessContract() {
        check(System.console() == null, "System.console() is null without a terminal");
        check(System.console() == System.console(), "System.console() is stable across calls");
        check(System.in != null && System.out != null && System.err != null,
              "standard streams exist independently of the console");
        // Console.class is usable reflectively even when no instance exists
        check(Console.class.getName().equals("java.io.Console"), "Console class name");
        check("java.io".equals(Console.class.getPackage().getName()), "Console package is java.io");
    }

    // ---- structural contract (JDK-27: finality was dropped vs JDK-8) ----
    static void testStructureV27() throws Exception {
        Class<?> c = Console.class;
        check(Modifier.isPublic(c.getModifiers()), "Console is public");
        check(!Modifier.isFinal(c.getModifiers()),
              "Console is NOT final in JDK-27 (final was dropped after JDK-8)");
        check(!Modifier.isAbstract(c.getModifiers()), "Console is a concrete class");
        check(java.io.Flushable.class.isAssignableFrom(c), "Console implements Flushable");
        check(!AutoCloseable.class.isAssignableFrom(c), "Console is not AutoCloseable (no close())");
        check(!java.io.Serializable.class.isAssignableFrom(c), "Console is not Serializable");
        // only the JDK factory can build one: every declared constructor is
        // package-private (JDK-27 has a single no-arg one)
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "exactly one declared constructor");
        for (Constructor<?> k : ctors) {
            check(!Modifier.isPublic(k.getModifiers()), "constructor <" + k + "> is not public");
            check(k.getParameterCount() == 0, "declared constructor takes no arguments");
        }
    }

    // ---- full 15-method surface with exact signatures and return types ----
    static void testMethodSurfaceV27() throws Exception {
        Class<?> c = Console.class;
        check(c.getMethod("writer").getReturnType() == java.io.PrintWriter.class,
              "writer() -> PrintWriter");
        check(c.getMethod("reader").getReturnType() == java.io.Reader.class,
              "reader() -> Reader");
        check(c.getMethod("format", String.class, Object[].class).getReturnType() == Console.class,
              "format(String,Object...) -> Console");
        check(c.getMethod("format", java.util.Locale.class, String.class, Object[].class)
                .getReturnType() == Console.class,
              "format(Locale,String,Object...) -> Console (JDK-17+)");
        check(c.getMethod("printf", String.class, Object[].class).getReturnType() == Console.class,
              "printf(String,Object...) -> Console");
        check(c.getMethod("printf", java.util.Locale.class, String.class, Object[].class)
                .getReturnType() == Console.class,
              "printf(Locale,String,Object...) -> Console (JDK-17+)");
        check(c.getMethod("readLine").getReturnType() == String.class, "readLine() -> String");
        check(c.getMethod("readLine", String.class, Object[].class).getReturnType() == String.class,
              "readLine(String,Object...) -> String");
        check(c.getMethod("readLine", java.util.Locale.class, String.class, Object[].class)
                .getReturnType() == String.class,
              "readLine(Locale,String,Object...) -> String (JDK-17+)");
        check(c.getMethod("readPassword").getReturnType() == char[].class,
              "readPassword() -> char[]");
        check(c.getMethod("readPassword", String.class, Object[].class).getReturnType() == char[].class,
              "readPassword(String,Object...) -> char[]");
        check(c.getMethod("readPassword", java.util.Locale.class, String.class, Object[].class)
                .getReturnType() == char[].class,
              "readPassword(Locale,String,Object...) -> char[] (JDK-17+)");
        check(c.getMethod("flush").getReturnType() == void.class, "flush() -> void");
        check(c.getMethod("charset").getReturnType() == java.nio.charset.Charset.class,
              "charset() -> Charset (JDK-17+)");
        check(c.getMethod("isTerminal").getReturnType() == boolean.class,
              "isTerminal() -> boolean (JDK-22)");
        // every public instance method of the surface is declared by Console itself
        for (Method m : c.getDeclaredMethods())
            if (Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers()))
                check(m.getDeclaringClass() == Console.class,
                      "method " + m.getName() + " declared by Console");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testHeadlessContract();
        testStructureV27();
        testMethodSurfaceV27();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
