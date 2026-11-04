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

        // ---- boundary/equivalence additions (M2) ----
        check(new Scanner("101").useRadix(2).nextInt() == 5, "binary radix 2 parse");
        check(new Scanner("11111").useRadix(2).nextInt() == 31, "binary radix 2 five bits");
        check(new Scanner("-42").hasNextInt() && new Scanner("-42").nextInt() == -42,
              "negative int token");
        check(new Scanner("1234567890123").nextLong() == 1234567890123L, "nextLong");
        check(new Scanner("3.5").nextDouble() == 3.5, "nextDouble");
        check(!new Scanner("").hasNext(), "empty hasNext false");
        assertThrows(IllegalArgumentException.class, () -> new Scanner("1").useRadix(1), "useRadix(1) IAE");
        assertThrows(IllegalArgumentException.class, () -> new Scanner("1").useRadix(37), "useRadix(37) IAE");
        Scanner ws = new Scanner("a\nb\tc");
        check(ws.next().equals("a") && ws.next().equals("b") && ws.next().equals("c") && !ws.hasNext(),
              "default whitespace tokens");
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

    // ---- 本棒新增：完整契约覆盖 ----
    static void testConstructorNullMatrix() {
        assertThrows(NullPointerException.class, () -> new Scanner((Readable) null),
                "Scanner((Readable)null) NPE");
        assertThrows(NullPointerException.class, () -> new Scanner((java.io.InputStream) null),
                "Scanner((InputStream)null) NPE");
        assertThrows(NullPointerException.class, () -> new Scanner((String) null),
                "Scanner((String)null) NPE");
        assertThrows(NullPointerException.class,
                () -> new Scanner((java.io.InputStream) null, "UTF-8"),
                "Scanner((InputStream)null,charset) NPE");
    }

    static void testNullSetters() {
        Scanner s = new Scanner("x");
        assertThrows(NullPointerException.class, () -> s.useDelimiter((String) null),
                "useDelimiter((String)null) NPE");
        assertThrows(NullPointerException.class, () -> s.useLocale(null), "useLocale(null) NPE");
    }

    static void testRemoveUnsupported() {
        assertThrows(UnsupportedOperationException.class, () -> new Scanner("a b").remove(),
                "remove() UOE");
    }

    static void testIoException() {
        Scanner s = new Scanner("1 2");
        check(s.ioException() == null, "ioException() null for String source");
        s.close();
    }

    static void testMatchAndToString() {
        Scanner s = new Scanner("hello 42");
        check(s.hasNext("hello"), "hasNext(pattern)");
        check("hello".equals(s.next("hello")), "next(pattern)");
        check(s.match() != null && "hello".equals(s.match().group()), "match() returns last match");
        check(s.toString() != null && s.toString().length() > 0, "toString non-empty");
        check(s.hasNextInt() && s.nextInt() == 42, "nextInt after match");
    }

    static void testFindWithinHorizonAndSkip() {
        Scanner s = new Scanner("abc123def");
        check("123".equals(s.findInLine("\\d+")), "findInLine digits");
        check("def".equals(s.findWithinHorizon("\\w+", 3)), "findWithinHorizon");

        Scanner s2 = new Scanner("skip these numbers 42");
        s2.skip("skip these numbers ");
        check(s2.nextInt() == 42, "skip(pattern) then nextInt");
    }

    static void testPrimitiveVariants() {
        Scanner s = new Scanner("42 7 9223372036854775807 true FALSE 3.25 255 12345678901234567890");
        check(s.nextByte() == 42, "nextByte");
        check(s.nextShort() == 7, "nextShort");
        check(s.nextLong() == Long.MAX_VALUE, "nextLong");
        check(s.nextBoolean(), "nextBoolean true");
        check(!s.nextBoolean(), "nextBoolean FALSE (case-insensitive)");
        s.useLocale(java.util.Locale.US);
        check(s.nextFloat() == 3.25f, "nextFloat");
        check(s.nextBigInteger().intValue() == 255, "nextBigInteger");
        check("12345678901234567890".equals(s.nextBigDecimal().toPlainString()), "nextBigDecimal");
    }

    static void testResetSemantics() {
        Scanner s = new Scanner("10");
        s.useRadix(16);
        s.useLocale(java.util.Locale.US);
        s.useDelimiter("X");
        s.reset();
        check(s.radix() == 10, "reset restores radix 10");
        check(s.locale().equals(java.util.Locale.getDefault()), "reset restores default locale");
        check(s.delimiter().pattern() != null, "reset restores default delimiter");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorNullMatrix();
        testNullSetters();
        testRemoveUnsupported();
        testIoException();
        testMatchAndToString();
        testFindWithinHorizonAndSkip();
        testPrimitiveVariants();
        testResetSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
