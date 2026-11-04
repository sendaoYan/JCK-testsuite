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

        // ---- boundary/equivalence additions (M2) ----
        StringTokenizer e0 = new StringTokenizer("", " ");
        check(e0.countTokens() == 0, "empty countTokens 0");
        check(!e0.hasMoreTokens(), "empty hasMoreTokens false");
        check(!e0.hasMoreElements(), "empty hasMoreElements false");
        assertThrows(NoSuchElementException.class, e0::nextToken, "empty nextToken NSEE");

        StringTokenizer onlyDelims = new StringTokenizer(",,,", ",");
        check(onlyDelims.countTokens() == 0 && !onlyDelims.hasMoreTokens(),
              "only delimiters produces no tokens");

        StringTokenizer consecutive = new StringTokenizer("a,,b", ",", false);
        check(consecutive.countTokens() == 2, "consecutive delimiters skipped");
        check("a".equals(consecutive.nextToken()) && "b".equals(consecutive.nextToken()),
              "skips empty tokens");

        StringTokenizer multi = new StringTokenizer("a:b:c", ":");
        check("a".equals(multi.nextToken()), "first token");
        check("b".equals(multi.nextToken(":")), "nextToken(delim) same delim");
        check("c".equals(multi.nextToken()) && !multi.hasMoreTokens(), "last token");

        StringTokenizer ws = new StringTokenizer("  leading and trailing  ");
        check(ws.countTokens() == 3, "default whitespace trims leading/trailing");
        check("leading".equals(ws.nextToken()) && "and".equals(ws.nextToken())
              && "trailing".equals(ws.nextToken()), "default whitespace tokens");
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

    static void testNextTokenSwitchDelim() {
        StringTokenizer st = new StringTokenizer("a-b+c", "-");
        check("a".equals(st.nextToken()), "token a with -");
        // switching delimiter set to "+": "-" becomes an ordinary character.
        check("-b".equals(st.nextToken("+")), "nextToken(+,) token -b");
        check("c".equals(st.nextToken()), "token c with +");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNextTokenSwitchDelim();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
