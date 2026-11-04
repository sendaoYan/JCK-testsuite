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
        testContracts();
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
