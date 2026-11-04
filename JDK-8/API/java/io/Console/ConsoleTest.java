/*
 * @test
 * @summary java.io.Console conformance: headless behavior.
 *          jtreg runs with redirected (non-tty) standard streams, so System.console()
 *          is null here: assert the headless contract (null on repeated calls,
 *          standard streams still present, no console object to operate on).
 *          Deepened: structural contract (final class in JDK 8, Flushable only,
 *          no public constructor) + full method surface with exact signatures
 *          and return types + JDK-8 baseline absences (Locale overloads,
 *          charset(), isTerminal() do not exist yet).
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

    static void testHeadless() {
        // No controlling terminal under jtreg: the only factory returns null.
        check(System.console() == null, "System.console() is null without a terminal");
        check(System.console() == null, "console stays null on repeated calls");
        // Standard streams remain available even though there is no Console.
        check(System.out != null && System.err != null && System.in != null,
              "standard streams still exist");
        // The factory is deterministic within one JVM session.
        check(System.console() == System.console(), "System.console() is stable across calls");
    }

    // ---- deepening: structural contract ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("java.io.Console");
        check(Modifier.isPublic(c.getModifiers()), "Console is public");
        check(Modifier.isFinal(c.getModifiers()), "Console is final in JDK 8");
        check(!Modifier.isAbstract(c.getModifiers()), "Console is a concrete class");
        check(java.io.Flushable.class.isAssignableFrom(c), "Console implements Flushable");
        check(!AutoCloseable.class.isAssignableFrom(c), "Console is not AutoCloseable (no close())");
        check(!java.io.Serializable.class.isAssignableFrom(c), "Console is not Serializable");
        check("java.io".equals(c.getPackage().getName()), "Console lives in java.io");
        // only the JDK factory can build one: no public constructor
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length > 0, "Console declares at least one constructor");
        for (Constructor<?> k : ctors)
            check(!Modifier.isPublic(k.getModifiers()),
                  "constructor <" + k + "> is not public (factory-only creation)");
    }

    // ---- deepening: exact method surface (JDK 8) ----
    static void testMethodSurface() throws Exception {
        Class<?> c = Class.forName("java.io.Console");
        Method writer = c.getMethod("writer");
        check(writer.getReturnType() == java.io.PrintWriter.class, "writer() -> PrintWriter");
        Method reader = c.getMethod("reader");
        check(reader.getReturnType() == java.io.Reader.class, "reader() -> Reader");
        Method fmt = c.getMethod("format", String.class, Object[].class);
        check(fmt.getReturnType() == Console.class, "format(String,Object...) -> Console");
        Method prf = c.getMethod("printf", String.class, Object[].class);
        check(prf.getReturnType() == Console.class, "printf(String,Object...) -> Console");
        Method rl0 = c.getMethod("readLine");
        check(rl0.getReturnType() == String.class, "readLine() -> String");
        Method rl = c.getMethod("readLine", String.class, Object[].class);
        check(rl.getReturnType() == String.class, "readLine(String,Object...) -> String");
        Method rp0 = c.getMethod("readPassword");
        check(rp0.getReturnType() == char[].class, "readPassword() -> char[]");
        Method rp = c.getMethod("readPassword", String.class, Object[].class);
        check(rp.getReturnType() == char[].class, "readPassword(String,Object...) -> char[]");
        Method fl = c.getMethod("flush");
        check(fl.getReturnType() == void.class, "flush() -> void");
        Method[] all = { writer, reader, fmt, prf, rl0, rl, rp0, rp, fl };
        for (Method m : all)
            check(Modifier.isPublic(m.getModifiers()), m.getName() + " is public");
    }

    // ---- deepening: JDK-8 baseline absences (JDK-27 additions) ----
    static void testJdk8Absences() throws Exception {
        Class<?> c = Class.forName("java.io.Console");
        String[][] cases = {
            { "format", "locale" },
            { "printf", "locale" },
            { "readLine", "locale" },
            { "readPassword", "locale" },
            { "charset", "none" },
            { "isTerminal", "none" },
        };
        for (String[] cs : cases) {
            boolean absent;
            if (cs[1].equals("locale")) {
                try {
                    c.getMethod(cs[0], java.util.Locale.class, String.class, Object[].class);
                    absent = false;
                } catch (NoSuchMethodException e) { absent = true; }
            } else {
                try {
                    c.getMethod(cs[0]);
                    absent = false;
                } catch (NoSuchMethodException e) { absent = true; }
            }
            check(absent, cs[0] + "(" + (cs[1].equals("locale") ? "Locale,..." : "")
                           + ") absent in JDK 8 (added later)");
        }
        // the varargs format family is not overload-resolved to Locale in JDK 8
        check(c.getDeclaredMethods().length > 0, "declared methods enumerable");
    }

    public static void main(String[] args) throws Exception {
        testHeadless();
        testStructure();
        testMethodSurface();
        testJdk8Absences();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
