/*
 * @test
 * @summary java.util.StringTokenizer API conformance: countTokens, hasMoreTokens, nextToken,
 *          nextToken(delim), and nextToken() after exhaustion throwing NoSuchElementException.
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

    public static void main(String[] args) {
        testTokens();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
