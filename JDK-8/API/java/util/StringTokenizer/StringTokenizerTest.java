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

    // ---- 契约深化 ----
    static void testContracts() {
        // null 输入 NPE
        try { new StringTokenizer(null); check(false, "null string NPE"); }
        catch (NullPointerException npe) { check(true, "null string NPE"); }
        // null delim 不在构造期校验：推迟到首次取词（双侧一致）
        try {
            StringTokenizer nd = new StringTokenizer("a", null);
            check(true, "null delims tolerated at construction");
            nd.nextToken();
            check(false, "null delims NPE deferred to use");
        } catch (NullPointerException npe) { check(true, "null delims NPE deferred to use"); }
        try { new StringTokenizer(null, ","); check(false, "null string 2-arg NPE"); }
        catch (NullPointerException npe) { check(true, "null string 2-arg NPE"); }
        try { new StringTokenizer(null, ",", true); check(false, "null string 3-arg NPE"); }
        catch (NullPointerException npe) { check(true, "null string 3-arg NPE"); }
        // 耗尽后 nextToken → NoSuchElementException
        StringTokenizer ex = new StringTokenizer("a");
        ex.nextToken();
        try { ex.nextToken(); check(false, "exhausted nextToken NSEE"); }
        catch (java.util.NoSuchElementException nsee) { check(true, "exhausted nextToken NSEE"); }
        try { ex.nextElement(); check(false, "exhausted nextElement NSEE"); }
        catch (java.util.NoSuchElementException nsee) { check(true, "exhausted nextElement NSEE"); }
        try { ex.nextToken(","); check(false, "exhausted nextToken(delims) NSEE"); }
        catch (java.util.NoSuchElementException nsee) { check(true, "exhausted nextToken(delims) NSEE"); }
        // nextToken(null) NPE
        StringTokenizer nn = new StringTokenizer("a b");
        try { nn.nextToken(null); check(false, "nextToken(null) NPE"); }
        catch (NullPointerException npe) { check(true, "nextToken(null) NPE"); }
        // 空分隔符字符串：没有分隔符，整串一个 token
        StringTokenizer noDelim = new StringTokenizer("a b", "");
        check(noDelim.countTokens() == 1 && "a b".equals(noDelim.nextToken()), "empty delims -> whole string");
        // countTokens 不推进
        StringTokenizer cnt = new StringTokenizer("a b c");
        check(cnt.countTokens() == 3 && cnt.countTokens() == 3, "countTokens does not consume");
        // 3 参构造 returnDelims=true 的完整序列
        StringTokenizer rd = new StringTokenizer("a,b", ",", true);
        StringBuilder seq = new StringBuilder();
        while (rd.hasMoreTokens()) seq.append(rd.nextToken());
        check("a,b".equals(seq.toString()), "returnDelims full sequence a , b");
        // 连续分隔符与 returnDelims
        StringTokenizer rd2 = new StringTokenizer(",,x", ",", true);
        seq.setLength(0);
        while (rd2.hasMoreTokens()) seq.append(rd2.nextToken());
        check(",,x".equals(seq.toString()), "adjacent delims each emitted");
        // nextToken(newDelims) 永久切换分隔符集合（后续调用沿用新集合）
        StringTokenizer sw = new StringTokenizer("a b|c");
        check("a".equals(sw.nextToken()), "first token under default delims");
        check(" b".equals(sw.nextToken("|")), "nextToken(delims) switches set, non-delim space kept in token");
        check("c".equals(sw.nextToken("|")), "continues with switched delims");
        try { sw.nextToken("|"); check(false, "exhausted after switch NSEE"); }
        catch (java.util.NoSuchElementException nsee) { check(true, "exhausted after switch NSEE"); }
        // Enumeration 视角
        StringTokenizer en = new StringTokenizer("1 2");
        int n = 0;
        while (en.hasMoreElements()) { en.nextElement(); n++; }
        check(n == 2, "Enumeration view count");
        check(!en.hasMoreElements(), "Enumeration exhausted");
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
        testContracts();
        testDefaultDelimiter();
        testReturnDelims();
        testNextTokenSwitchDelim();
        testEnumeration();
        testEmptyAndNoDelims();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
