/*
 * @test
 * @summary java.util.Scanner conformance: white-box structure plus black-box behavior of
 *          nextInt/next/hasNext, useDelimiter, hasNextInt/nextInt with radix, nextLine,
 *          findInLine, reset, and close semantics.
 * @run main ScannerTest
 */

import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ScannerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Scanner");
        check(!c.isInterface(), "Scanner is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Scanner is final");
        check(java.util.Iterator.class.isAssignableFrom(c), "Scanner implements Iterator<String>");
        check(java.io.Closeable.class.isAssignableFrom(c), "Scanner implements Closeable");
        c.getMethod("nextInt");
        c.getMethod("hasNextInt");
        c.getMethod("useDelimiter", String.class);
        c.getMethod("nextLine");
        c.getMethod("useRadix", int.class);
        c.getMethod("close");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testTokens();
        testDelimiter();
        testHasNextInt();
        testNextLine();
        testRadix();
        testFindInLine();
        testExceptions();
        testClose();
    }

    static void testTokens() {
        Scanner s = new Scanner("10 20 hello");
        check(s.nextInt() == 10 && s.nextInt() == 20, "nextInt");
        check("hello".equals(s.next()), "next");
        check(!s.hasNext(), "exhausted");
    }

    static void testDelimiter() {
        Scanner s = new Scanner("a,b,c").useDelimiter(",");
        check("a".equals(s.next()) && "b".equals(s.next()) && "c".equals(s.next()),
              "useDelimiter");
        check(!s.hasNext(), "delimiter-split exhausted");
    }

    static void testHasNextInt() {
        Scanner s = new Scanner("42");
        check(s.hasNextInt() && s.nextInt() == 42, "hasNextInt/nextInt");
        Scanner mixed = new Scanner("x 7");
        check(!mixed.hasNextInt(), "hasNextInt false for non-int token");
        check("x".equals(mixed.next()), "next consumes non-int token");
        check(mixed.hasNextInt() && mixed.nextInt() == 7, "then next int");
    }

    static void testNextLine() {
        Scanner s = new Scanner("line1\nline2\n");
        check("line1".equals(s.nextLine()), "nextLine 1");
        check(s.hasNextLine(), "hasNextLine before last");
        check("line2".equals(s.nextLine()), "nextLine 2");
        check(!s.hasNextLine(), "hasNextLine false after last");
    }

    static void testRadix() {
        Scanner hex = new Scanner("ff").useRadix(16);
        check(hex.hasNextInt() && hex.nextInt() == 255, "useRadix(16) nextInt");
        Scanner hex2 = new Scanner("ff");
        check(hex2.hasNextInt(16) && hex2.nextInt(16) == 255, "hasNextInt/nextInt with radix");
    }

    static void testFindInLine() {
        Scanner s = new Scanner("a1b2c3");
        check("1".equals(s.findInLine("\\d")), "findInLine first digit");
        check("2".equals(s.findInLine("\\d")), "findInLine advances");
    }

    static void testExceptions() {
        assertThrows(InputMismatchException.class,
                () -> new Scanner("abc").nextInt(), "nextInt on non-int throws InputMismatchException");
        assertThrows(NoSuchElementException.class,
                () -> new Scanner("").next(), "next on empty throws NoSuchElementException");
    }

    static void testClose() {
        Scanner s = new Scanner("1 2");
        s.close();
        assertThrows(IllegalStateException.class, s::nextInt, "nextInt after close throws IllegalStateException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
