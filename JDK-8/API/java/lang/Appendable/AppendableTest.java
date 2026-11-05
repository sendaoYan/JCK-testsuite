/*
 * @test
 * @summary java.lang.Appendable API conformance: append(CharSequence), append(CharSequence,
 *          int,int), and append(char) return the receiver and append in order; index bounds
 *          on the (csq,start,end) overload.
 * @run main AppendableTest
 */

public class AppendableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAppend() throws Exception {
        Appendable sb = new StringBuilder();
        check(sb.append("ab") == sb, "append(csq) returns this");
        check(sb.append("cd", 0, 2).toString().equals("abcd"), "append(csq,start,end)");
        check(sb.append('e').toString().equals("abcde"), "append(char)");
        check(sb.append(null).toString().equals("abcdenull"), "append(null csq) appends \"null\"");
    }

    static void testBounds() throws Exception {
        Appendable sb = new StringBuilder();
        try {
            sb.append("ab", -1, 1);
            throw new AssertionError("append(csq,-1,1): expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
        try {
            sb.append("ab", 1, 3);
            throw new AssertionError("append(csq,1,3): expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
        try {
            sb.append("ab", 2, 1);
            throw new AssertionError("append(csq,2,1): expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
    }

    // ===================== 契约深化（2026-11）追加 =====================
    // 三重载路由实证 + null 子区间语义 + 精确越界消息（8/27 形式不同，MODERN 分支）+
    // StringBuilder/StringBuffer/CharBuffer/StringWriter 多实现一致性。

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");

    /** 记录每个重载的路由次序，证明三个 append 各自独立路由。 */
    static final class Router implements Appendable {
        final StringBuilder log = new StringBuilder();
        final StringBuilder buf = new StringBuilder();
        public Appendable append(CharSequence csq) {
            log.append("csq(").append(csq).append(");");
            buf.append(csq);
            return this;
        }
        public Appendable append(CharSequence csq, int start, int end) {
            log.append("sub(").append(csq).append(':').append(start).append(',').append(end).append(");");
            if (csq == null) buf.append("null", start, end);
            else buf.append(csq, start, end);
            return this;
        }
        public Appendable append(char c) {
            log.append("char(").append(c).append(");");
            buf.append(c);
            return this;
        }
        public String toString() { return buf.toString(); }
    }

    static void expectExactMsg(Class<? extends Throwable> type, Runnable r,
                               String modernMsg, String legacyMsg, String label) {
        try {
            r.run();
        } catch (Throwable t) {
            if (!type.isInstance(t)) {
                throw new AssertionError(label + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getName(), t);
            }
            String want = MODERN ? modernMsg : legacyMsg;
            if (!java.util.Objects.equals(want, t.getMessage())) {
                throw new AssertionError(label + ": message <" + t.getMessage()
                        + "> != expected <" + want + ">");
            }
            passed++;
            return;
        }
        throw new AssertionError(label + ": expected " + type.getSimpleName()
                + " but nothing was thrown");
    }

    static void testOverloadRouting() throws Exception {
        Router r = new Router();
        check(r.append("A") == r, "append(csq) returns this");
        check(r.append("BC", 1, 2) == r, "append(csq,s,e) returns this");
        check(r.append('D') == r, "append(char) returns this");
        check(r.toString().equals("ACD"), "three overloads route in order");
        check(r.log.toString().equals("csq(A);sub(BC:1,2);char(D);"),
              "each overload routed distinctly");
        r.append((CharSequence) null);
        check(r.toString().equals("ACDnull"), "append(null) routes to csq overload appending \"null\"");
        check(r.log.toString().equals("csq(A);sub(BC:1,2);char(D);csq(null);"),
              "null csq routed to append(csq), not append(csq,s,e)");
        StringBuilder seq = new StringBuilder();
        seq.append(new StringBuffer("SB"));
        check(seq.toString().equals("SB"), "append accepts StringBuffer as csq");
    }

    static void testNullSubrange() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append((CharSequence) null, 1, 3);
        check(sb.toString().equals("ul"), "append(null,1,3) slices the literal \"null\"");
        StringBuilder full = new StringBuilder();
        full.append((CharSequence) null, 0, 4);
        check(full.toString().equals("null"), "append(null,0,4) == \"null\"");
        Router r = new Router();
        r.append((CharSequence) null, 0, 4);
        check(r.toString().equals("null"), "custom impl: append(null,0,4) == \"null\"");
        expectExactMsg(IndexOutOfBoundsException.class,
                () -> new StringBuilder().append((CharSequence) null, 0, 5),
                "Range [0, 5) out of bounds for length 4",
                "start 0, end 5, s.length() 4",
                "append(null,0,5)");
    }

    static void testExactBoundMessages() {
        expectExactMsg(IndexOutOfBoundsException.class,
                () -> new StringBuilder().append("ab", -1, 1),
                "Range [-1, 1) out of bounds for length 2",
                "start -1, end 1, s.length() 2",
                "append(csq,-1,1)");
        expectExactMsg(IndexOutOfBoundsException.class,
                () -> new StringBuilder().append("ab", 1, 3),
                "Range [1, 3) out of bounds for length 2",
                "start 1, end 3, s.length() 2",
                "append(csq,1,3)");
        expectExactMsg(IndexOutOfBoundsException.class,
                () -> new StringBuilder().append("ab", 2, 1),
                "Range [2, 1) out of bounds for length 2",
                "start 2, end 1, s.length() 2",
                "append(csq,2,1)");
    }

    static void testCharBufferAsAppendable() throws Exception {
        java.nio.CharBuffer cb = java.nio.CharBuffer.allocate(4);
        Appendable a = cb;
        check(a.append('x') == a, "CharBuffer append(char) returns this");
        check(a.append("yz") == a, "CharBuffer append(csq) returns this");
        a.append("AB", 1, 2);
        check(cb.flip().toString().equals("xyzB"), "CharBuffer accumulates all three overloads");
    }

    static void testStringWriterAsAppendable() throws Exception {
        java.io.StringWriter sw = new java.io.StringWriter();
        Appendable w = sw;
        check(w.append("ab") == w, "Writer append(csq) returns this (Writer returns Writer)");
        check(w.append('c') == w, "Writer append(char) returns this");
        check(w.append("xy", 1, 2) == w, "Writer append(csq,s,e) returns this");
        check(sw.toString().equals("abcy"), "StringWriter content built via interface");
    }

    static void testStringBufferReceiver() throws Exception {
        StringBuffer buf = new StringBuffer();
        Appendable a = buf;
        check(a.append("a") == a, "StringBuffer append(csq) receiver identity");
        check(a.append("bc", 0, 2) == a, "StringBuffer append(csq,s,e) receiver identity");
        check(a.append('d') == a, "StringBuffer append(char) receiver identity");
        check(buf.toString().equals("abcd"), "StringBuffer content via interface");
    }

    static void testEmptyRangeAndEdges() throws Exception {
        StringBuilder sb = new StringBuilder("pre");
        sb.append("abc", 1, 1);
        check(sb.toString().equals("pre"), "empty subrange appends nothing");
        StringBuilder sb2 = new StringBuilder();
        sb2.append('\u0000');
        check(sb2.length() == 1 && sb2.charAt(0) == '\u0000', "append(char) accepts NUL");
        sb2.append(Character.MAX_VALUE);
        check(sb2.length() == 2 && sb2.charAt(1) == Character.MAX_VALUE, "append(char) accepts MAX_VALUE");
        StringBuilder sb3 = new StringBuilder();
        CharSequence csq = new StringBuilder("HI");
        sb3.append(csq);
        check(sb3.toString().equals("HI"), "append takes any CharSequence (builder as argument)");
        StringBuilder sb4 = new StringBuilder();
        sb4.append("hello", 1, 3);
        check(sb4.toString().equals("hello".substring(1, 3)),
              "append(csq,s,e) == append(csq.subSequence(s,e))");
    }

    static void testIoExceptionContract() throws Exception {
        final java.io.IOException boom = new java.io.IOException("boom");
        Appendable failing = new Appendable() {
            public Appendable append(CharSequence csq) throws java.io.IOException { throw boom; }
            public Appendable append(CharSequence csq, int s, int e) throws java.io.IOException { throw boom; }
            public Appendable append(char c) throws java.io.IOException { throw boom; }
        };
        checkThrowsSame(boom, () -> failing.append("x"), "append(csq) propagates IOException");
        checkThrowsSame(boom, () -> failing.append("x", 0, 1), "append(csq,s,e) propagates IOException");
        checkThrowsSame(boom, () -> failing.append('x'), "append(char) propagates IOException");
    }

    static void checkThrowsSame(final java.io.IOException expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t == expected) { passed++; return; }
            throw new AssertionError(msg + ": got " + t, t);
        }
        throw new AssertionError(msg + ": nothing was thrown");
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    public static void main(String[] args) throws Exception {
        testAppend();
        testBounds();
        testOverloadRouting();
        testNullSubrange();
        testExactBoundMessages();
        testCharBufferAsAppendable();
        testStringWriterAsAppendable();
        testStringBufferReceiver();
        testEmptyRangeAndEdges();
        testIoExceptionContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
