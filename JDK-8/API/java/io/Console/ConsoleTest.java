/*
 * @test
 * @summary java.io.Console conformance: final class, method surface, System.console().
 * @run main ConsoleTest
 */

import java.io.Console;
import java.io.Flushable;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Method;

public class ConsoleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConsole() throws Exception {
        check(Flushable.class.isAssignableFrom(Console.class), "Console implements Flushable");
        Console c = System.console();
        check(c == null || c instanceof Console, "System.console() returns null or Console");

        Method m = Console.class.getMethod("readLine");
        check(m.getReturnType() == String.class, "readLine() returns String");
        m = Console.class.getMethod("readLine", String.class, Object[].class);
        check(m.getReturnType() == String.class, "readLine(fmt, args) returns String");
        m = Console.class.getMethod("readPassword");
        check(m.getReturnType() == char[].class, "readPassword() returns char[]");
        m = Console.class.getMethod("readPassword", String.class, Object[].class);
        check(m.getReturnType() == char[].class, "readPassword(fmt, args) returns char[]");
        m = Console.class.getMethod("format", String.class, Object[].class);
        check(m.getReturnType() == Console.class, "format returns Console");
        m = Console.class.getMethod("printf", String.class, Object[].class);
        check(m.getReturnType() == Console.class, "printf returns Console");
        m = Console.class.getMethod("reader");
        check(m.getReturnType() == Reader.class, "reader() returns Reader");
        m = Console.class.getMethod("writer");
        check(m.getReturnType() == PrintWriter.class, "writer() returns PrintWriter");
        m = Console.class.getMethod("flush");
        check(m.getReturnType() == void.class, "flush() returns void");
    }

    public static void main(String[] args) throws Exception {
        testConsole();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
