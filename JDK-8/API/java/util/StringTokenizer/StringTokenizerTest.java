/*
 * @test
 * @summary java.util.StringTokenizer API conformance (Java 8 surface): complete contract coverage
 *          of the 3 constructors (default whitespace delim, custom delim, returnDelims), token
 *          counting/hasMoreTokens/nextToken, nextToken(delim) switch, returnDelims tokens,
 *          Enumeration hasMoreElements/nextElement, and nextToken/nextElement exhaustion NSEE.
 * @run main StringTokenizerTest
 */

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class StringTokenizerTest {
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
        StringTokenizer st = new StringTokenizer("a b c", " ");
        check(st.countTokens() == 3, "countTokens");
        check(st.hasMoreTokens(), "hasMoreTokens");
        check("a".equals(st.nextToken()), "nextToken 1");
        check("b".equals(st.nextToken(" ")), "nextToken(delim)");
        check("c".equals(st.nextToken()) && !st.hasMoreTokens(), "nextToken 3 / exhausted");
        try {
            st.nextToken();
            throw new AssertionError("nextToken exhausted: expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
            passed++;
        }
    }

    static void testDefaultDelimiter() {
        // default constructor splits on any whitespace.
        StringTokenizer st = new StringTokenizer("a\nb\tc ");
        check(st.countTokens() == 3, "default whitespace countTokens");
        check("a".equals(st.nextToken()) && "b".equals(st.nextToken())
              && "c".equals(st.nextToken()), "default whitespace tokens");
    }

    static void testReturnDelims() {
        StringTokenizer st = new StringTokenizer("a,b,c", ",", true);
        check(st.countTokens() == 5, "returnDelims countTokens");
        check("a".equals(st.nextToken()), "token a");
        check(",".equals(st.nextToken()), "delim ,");
        check("b".equals(st.nextToken()), "token b");
        check(",".equals(st.nextToken()), "delim ,");
        check("c".equals(st.nextToken()), "token c");
    }

    static void testNextTokenSwitchDelim() {
        StringTokenizer st = new StringTokenizer("a-b+c", "-");
        check("a".equals(st.nextToken()), "token a with -");
        // switching delimiter set to "+" for subsequent tokens: the "-" is now
        // an ordinary character, so the next token is "-b".
        check("-b".equals(st.nextToken("+")), "nextToken(+,) token -b");
        check("c".equals(st.nextToken()), "token c with +");
    }

    static void testEnumeration() {
        StringTokenizer st = new StringTokenizer("x y", " ");
        check(st.hasMoreElements(), "hasMoreElements");
        check("x".equals(st.nextElement()), "nextElement 1");
        check(st.hasMoreElements(), "hasMoreElements 2");
        check("y".equals(st.nextElement()), "nextElement 2");
        check(!st.hasMoreElements(), "hasMoreElements exhausted");
        expectThrows(NoSuchElementException.class, () -> st.nextElement(),
                     "nextElement exhausted NSEE");
    }

    static void testEmptyAndNoDelims() {
        check(!new StringTokenizer("").hasMoreTokens(), "empty string no tokens");
        check(new StringTokenizer("abc", "x").countTokens() == 1,
              "no delimiter present -> one token");
        check("abc".equals(new StringTokenizer("abc", "x").nextToken()),
              "no delimiter -> whole string token");
    }

    public static void main(String[] args) {
        testTokens();
        testDefaultDelimiter();
        testReturnDelims();
        testNextTokenSwitchDelim();
        testEnumeration();
        testEmptyAndNoDelims();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
