/*
 * @test
 * @summary java.util.StringTokenizer conformance: white-box structure plus black-box behavior
 *          of countTokens/hasMoreTokens/nextToken/nextToken(delim), returnDelims constructor,
 *          hasMoreElements/nextElement, and exhaustion exceptions.
 * @run main StringTokenizerTest
 */

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class StringTokenizerTest {
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
        Class<?> c = Class.forName("java.util.StringTokenizer");
        check(!c.isInterface(), "StringTokenizer is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "StringTokenizer is concrete");
        check(Enumeration.class.isAssignableFrom(c), "StringTokenizer implements Enumeration");
        c.getMethod("countTokens");
        c.getMethod("hasMoreTokens");
        c.getMethod("nextToken");
        c.getMethod("nextToken", String.class);
        c.getMethod("hasMoreElements");
        c.getMethod("nextElement");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testTokens();
        testDefaultDelimiters();
        testReturnDelims();
        testEnumeration();
    }

    static void testTokens() {
        StringTokenizer st = new StringTokenizer("a b c", " ");
        check(st.countTokens() == 3, "countTokens");
        check(st.hasMoreTokens(), "hasMoreTokens");
        check("a".equals(st.nextToken()), "nextToken 1");
        check("b".equals(st.nextToken(" ")), "nextToken(delim)");
        check("c".equals(st.nextToken()) && !st.hasMoreTokens(), "nextToken 3 / exhausted");
        assertThrows(NoSuchElementException.class, st::nextToken,
                "nextToken exhausted throws NoSuchElementException");
    }

    static void testDefaultDelimiters() {
        StringTokenizer st = new StringTokenizer("x\ty\nz"); // default delim: whitespace
        check(st.countTokens() == 3, "default whitespace delimiters count");
        check("x".equals(st.nextToken()) && "y".equals(st.nextToken())
              && "z".equals(st.nextToken()), "default whitespace delimiters tokens");
    }

    static void testReturnDelims() {
        StringTokenizer st = new StringTokenizer("a,b", ",", true);
        check("a".equals(st.nextToken()), "token a");
        check(",".equals(st.nextToken()), "delimiter returned");
        check("b".equals(st.nextToken()), "token b");
        check(!st.hasMoreTokens(), "returnDelims exhausted");
    }

    static void testEnumeration() {
        StringTokenizer st = new StringTokenizer("1 2", " ");
        check(st.hasMoreElements(), "hasMoreElements");
        check("1".equals(st.nextElement()), "nextElement 1");
        check("2".equals(st.nextElement()), "nextElement 2");
        assertThrows(NoSuchElementException.class, st::nextElement,
                "nextElement exhausted throws NoSuchElementException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
