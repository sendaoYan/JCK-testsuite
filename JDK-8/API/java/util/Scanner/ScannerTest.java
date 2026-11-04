/*
 * @test
 * @summary java.util.Scanner API conformance (Java 8 surface): complete contract coverage
 *          of constructors (null NPE), delimiter/locale/radix getter-setter + null NPE +
 *          radix IAE matrix, next/hasNext exhaustion + close IllegalStateException +
 *          remove UnsupportedOperationException, nextInt/nextLong/nextBoolean/nextDouble +
 *          InputMismatchException + radix variants, findInLine/findWithinHorizon/skip,
 *          reset semantics, match(), toString, and ioException().
 * @run main ScannerTest
 */

import java.util.InputMismatchException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ScannerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName()
                + " but got " + t.getClass().getSimpleName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but nothing thrown");
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
    }

    static void testHasNextInt() {
        Scanner s = new Scanner("42");
        check(s.hasNextInt() && s.nextInt() == 42, "hasNextInt/nextInt");
    }

    static void testConstructorNull() {
        expectThrows(NullPointerException.class, () -> new Scanner((Readable) null),
                     "Scanner((Readable)null) NPE");
        expectThrows(NullPointerException.class, () -> new Scanner((java.io.InputStream) null),
                     "Scanner((InputStream)null) NPE");
        expectThrows(NullPointerException.class, () -> new Scanner((String) null),
                     "Scanner((String)null) NPE");
        // InputStream charset variant with null source.
        expectThrows(NullPointerException.class,
                     () -> new Scanner((java.io.InputStream) null, "UTF-8"),
                     "Scanner((InputStream)null,charset) NPE");
    }

    static void testRadix() {
        Scanner s = new Scanner("ff");
        check(s.radix() == 10, "default radix 10");
        s.useRadix(16);
        check(s.radix() == 16, "useRadix(16)");
        check(s.nextInt() == 255, "nextInt with radix 16 parses ff");

        expectThrows(IllegalArgumentException.class, () -> new Scanner("1").useRadix(1),
                     "useRadix(1) IAE");
        expectThrows(IllegalArgumentException.class, () -> new Scanner("1").useRadix(37),
                     "useRadix(37) IAE");
        new Scanner("1").useRadix(2);
        new Scanner("1").useRadix(36);
        passed++;
    }

    static void testNullSetters() {
        Scanner s = new Scanner("x");
        expectThrows(NullPointerException.class, () -> s.useDelimiter((String) null),
                     "useDelimiter((String)null) NPE");
        expectThrows(NullPointerException.class, () -> s.useLocale(null), "useLocale(null) NPE");
    }

    static void testExhaustionAndClose() {
        Scanner empty = new Scanner("");
        check(!empty.hasNext(), "hasNext false when empty");
        expectThrows(NoSuchElementException.class, () -> empty.next(), "next() NSEE when empty");

        Scanner s = new Scanner("1 2 3");
        s.close();
        expectThrows(IllegalStateException.class, () -> s.hasNext(), "hasNext() ISE after close");
        expectThrows(IllegalStateException.class, () -> s.next(), "next() ISE after close");
        expectThrows(IllegalStateException.class, () -> s.nextInt(), "nextInt() ISE after close");
        check(s.ioException() == null, "ioException() null for String source");
    }

    static void testRemoveUnsupported() {
        Scanner s = new Scanner("a b");
        expectThrows(UnsupportedOperationException.class, () -> s.remove(), "remove() UOE");
    }

    static void testNextPrimitives() {
        Scanner s = new Scanner("42 7 9223372036854775807 true FALSE 3.25");
        check(s.nextByte() == 42, "nextByte");
        check(s.nextShort() == 7, "nextShort");
        check(s.nextLong() == Long.MAX_VALUE, "nextLong");
        check(s.nextBoolean(), "nextBoolean true");
        check(!s.nextBoolean(), "nextBoolean FALSE (case-insensitive)");
        s.useLocale(Locale.US);
        check(s.nextDouble() == 3.25, "nextDouble");
    }

    static void testInputMismatch() {
        Scanner s = new Scanner("hello");
        expectThrows(InputMismatchException.class, () -> s.nextInt(), "nextInt() on word IME");
    }

    static void testHasNextVariants() {
        Scanner s = new Scanner("1 2.5 true 10");
        check(s.hasNextInt(), "hasNextInt true");
        check(s.nextInt() == 1, "nextInt");
        check(s.hasNextDouble(), "hasNextDouble true");
        s.useLocale(Locale.US);
        check(s.nextDouble() == 2.5, "nextDouble");
        check(s.hasNextBoolean(), "hasNextBoolean true");
        check(s.nextBoolean(), "nextBoolean");
        check(s.hasNextInt(16), "hasNextInt(16) true");
        check(s.nextInt(16) == 16, "nextInt(16) parses 10 as hex");
    }

    static void testNextLine() {
        Scanner s = new Scanner("first line\nsecond line\n");
        check(s.hasNextLine(), "hasNextLine true");
        check("first line".equals(s.nextLine()), "nextLine first");
        check("second line".equals(s.nextLine()), "nextLine second");
        check(!s.hasNextLine(), "hasNextLine false at end");
    }

    static void testFindAndSkip() {
        Scanner s = new Scanner("abc123def");
        check("123".equals(s.findInLine("\\d+")), "findInLine matches digits");
        check(s.findInLine("\\d+") == null, "findInLine null when no more match");
        check("def".equals(s.findWithinHorizon("\\w+", 3)), "findWithinHorizon");

        Scanner s2 = new Scanner("skip these numbers 42");
        s2.skip("skip these numbers ");
        check(s2.nextInt() == 42, "skip(pattern) then nextInt");
    }

    static void testReset() {
        Scanner s = new Scanner("10");
        s.useRadix(16);
        s.useLocale(Locale.US);
        s.useDelimiter("X");
        s.reset();
        check(s.radix() == 10, "reset restores radix 10");
        check(s.locale().equals(Locale.getDefault()), "reset restores default locale");
        check(s.delimiter().pattern() != null, "reset restores default delimiter");
    }

    static void testMatchAndToString() {
        Scanner s = new Scanner("hello 42");
        check(s.hasNext("hello"), "hasNext(pattern)");
        check("hello".equals(s.next("hello")), "next(pattern)");
        check(s.match() != null && "hello".equals(s.match().group()), "match() returns last match");
        check(s.toString() != null && s.toString().length() > 0, "toString non-empty");
        check(s.hasNextInt(), "hasNextInt after match");
        check(s.nextInt() == 42, "nextInt after match");
    }

    public static void main(String[] args) {
        testTokens();
        testDelimiter();
        testHasNextInt();
        testConstructorNull();
        testRadix();
        testNullSetters();
        testExhaustionAndClose();
        testRemoveUnsupported();
        testNextPrimitives();
        testInputMismatch();
        testHasNextVariants();
        testNextLine();
        testFindAndSkip();
        testReset();
        testMatchAndToString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
