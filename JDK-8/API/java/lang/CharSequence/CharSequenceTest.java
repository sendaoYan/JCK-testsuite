/*
 * @test
 * @summary java.lang.CharSequence API conformance: length/charAt/subSequence/toString on
 *          String, charAt index bounds (StringIndexOutOfBoundsException), subSequence
 *          bounds, and default chars()/codePoints() streams.
 * @run main CharSequenceTest
 */

public class CharSequenceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testStringAsCharSequence() {
        CharSequence s = "hello";
        check(s.length() == 5, "length");
        check(s.charAt(0) == 'h' && s.charAt(4) == 'o', "charAt");
        check(s.subSequence(1, 4).toString().equals("ell"), "subSequence");
        check(s.toString().equals("hello"), "toString returns the string");
        check(s.subSequence(0, s.length()) == s, "subSequence(0,length) == this");
    }

    static void testCharAtBounds() {
        CharSequence s = "abc";
        try {
            s.charAt(-1);
            throw new AssertionError("charAt(-1): expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
            passed++;
        }
        try {
            s.charAt(3);
            throw new AssertionError("charAt(3): expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
            passed++;
        }
    }

    static void testSubSequenceBounds() {
        CharSequence s = "abc";
        try {
            s.subSequence(-1, 2);
            throw new AssertionError("subSequence(-1,2): expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
            passed++;
        }
        try {
            s.subSequence(1, 4);
            throw new AssertionError("subSequence(1,4): expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
            passed++;
        }
        try {
            s.subSequence(2, 1);
            throw new AssertionError("subSequence(2,1): expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
            passed++;
        }
    }

    static void testStreams() {
        CharSequence s = "ab";
        check(s.chars().count() == 2, "chars() count");
        check(s.codePoints().count() == 2, "codePoints() count");
        check(s.chars().sum() == 'a' + 'b', "chars() sum == a+b");
    }

    // ===================== 契约深化（2026-11）追加 =====================
    // 自定义实现 + String/StringBuilder/StringBuffer/CharBuffer 多态一致性 +
    // 默认 chars()/codePoints() 经 charAt 路由的实证 + 接口文档契约负例。

    /** 最小 CharSequence 实现（不覆盖任何默认方法）。 */
    static final class Seq implements CharSequence {
        final String s;
        Seq(String s) { this.s = s; }
        public int length() { return s.length(); }
        public char charAt(int i) { return s.charAt(i); }
        public CharSequence subSequence(int a, int b) { return s.substring(a, b); }
        public String toString() { return s; }
    }

    /** 统计 charAt 调用次数，证明默认流方法逐索引路由到 charAt。 */
    static final class CountingSeq implements CharSequence {
        final String s;
        int charAtCalls;
        CountingSeq(String s) { this.s = s; }
        public int length() { return s.length(); }
        public char charAt(int i) { charAtCalls++; return s.charAt(i); }
        public CharSequence subSequence(int a, int b) { return s.substring(a, b); }
        public String toString() { return s; }
    }

    static void expectIOOBE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
    }

    static void testCustomImplContract() {
        Seq seq = new Seq("hello");
        check(seq.length() == 5, "Seq length");
        check(seq.charAt(0) == 'h' && seq.charAt(4) == 'o', "Seq charAt endpoints");
        check(seq.subSequence(1, 3).toString().equals("el"), "Seq subSequence(1,3)");
        check(seq.toString().equals("hello"), "Seq toString");
        check(seq.subSequence(0, 5).toString().equals("hello"), "Seq subSequence full window");
        check(seq.subSequence(2, 2).toString().isEmpty(), "Seq subSequence(2,2) empty");
        CharSequence inner = seq.subSequence(1, 4);
        check(inner.subSequence(0, 2).toString().equals("el"), "chained subSequence");
        check(inner.length() == 3, "subSequence length");
        check(inner.charAt(2) == 'l', "subSequence charAt relative index");
        Seq empty = new Seq("");
        check(empty.length() == 0 && empty.toString().isEmpty(), "empty Seq");
        Seq one = new Seq("x");
        check(one.charAt(0) == 'x' && one.subSequence(0, 1).toString().equals("x"), "single-char Seq");
    }

    static void testPolymorphicImpls() {
        CharSequence[] impls = {
            "hello",
            new StringBuilder("hello"),
            new StringBuffer("hello"),
            java.nio.CharBuffer.wrap("hello"),
            new Seq("hello")
        };
        for (CharSequence cs : impls) {
            check(cs.length() == 5, cs.getClass().getSimpleName() + " length");
            check(cs.charAt(0) == 'h' && cs.charAt(4) == 'o', cs.getClass().getSimpleName() + " charAt");
            check(cs.toString().equals("hello"), cs.getClass().getSimpleName() + " toString");
            check(cs.subSequence(1, 4).toString().equals("ell"),
                  cs.getClass().getSimpleName() + " subSequence(1,4)");
            StringBuilder acc = new StringBuilder();
            for (int i = 0; i < cs.length(); i++) acc.append(cs.charAt(i));
            check(acc.toString().equals("hello"),
                  cs.getClass().getSimpleName() + " charAt loop equals toString");
        }
        // 以接口类型调用 String 的特化 subSequence(0,len) == this。
        CharSequence s = "hello";
        check(s.subSequence(0, 5) == s, "String subSequence(0,len) returns this via interface");
    }

    static void testDefaultStreamRouting() {
        CountingSeq c = new CountingSeq("abc");
        int[] arr = c.chars().toArray();
        check(arr.length == 3 && arr[0] == 'a' && arr[1] == 'b' && arr[2] == 'c',
              "default chars() toArray exact");
        check(c.charAtCalls == 3, "default chars() calls charAt once per index (locked)");
        CountingSeq c2 = new CountingSeq("abc");
        check(c2.chars().sum() == 'a' + 'b' + 'c', "default chars() sum");
        String sup = new String(new int[]{0x1D11E, 'a'}, 0, 2);
        CountingSeq cs = new CountingSeq(sup);
        int[] units = cs.chars().toArray();
        check(units.length == 3 && units[0] == 0xD834 && units[1] == 0xDD1E && units[2] == 'a',
              "default chars() yields UTF-16 units over surrogate pair");
        check(cs.charAtCalls == 3, "chars() over pair: charAt called for each UTF-16 unit");
        CountingSeq cp = new CountingSeq(sup);
        int[] cps = cp.codePoints().toArray();
        check(cps.length == 2 && cps[0] == 0x1D11E && cps[1] == 'a',
              "default codePoints() decodes surrogate pairs");
        check(cp.charAtCalls == 3, "codePoints() calls charAt once per UTF-16 unit");
        CountingSeq blank = new CountingSeq("");
        check(blank.chars().count() == 0 && blank.codePoints().count() == 0,
              "empty Seq streams count 0");
        // CharSequence 引用上的流（String 自有实现，不经 charAt）。
        CharSequence str = "ab";
        check(str.chars().sum() == 'a' + 'b', "String chars() via CharSequence reference");
        check(new StringBuilder("ab").chars().count() == 2, "StringBuilder chars() count");
    }

    static void testImplBounds() {
        Seq seq = new Seq("abc");
        expectIOOBE(() -> seq.charAt(-1), "Seq charAt(-1)");
        expectIOOBE(() -> seq.charAt(3), "Seq charAt(len)");
        expectIOOBE(() -> seq.subSequence(-1, 2), "Seq subSequence(-1,2)");
        expectIOOBE(() -> seq.subSequence(1, 4), "Seq subSequence(1,4)");
        expectIOOBE(() -> seq.subSequence(2, 1), "Seq subSequence(2,1)");
        StringBuilder sb = new StringBuilder("abc");
        expectIOOBE(() -> sb.charAt(-1), "StringBuilder charAt(-1)");
        expectIOOBE(() -> sb.charAt(3), "StringBuilder charAt(len)");
        expectIOOBE(() -> sb.subSequence(1, 4), "StringBuilder subSequence(1,4)");
        StringBuffer buf = new StringBuffer("abc");
        expectIOOBE(() -> buf.charAt(3), "StringBuffer charAt(len)");
        expectIOOBE(() -> buf.subSequence(-1, 1), "StringBuffer subSequence(-1,1)");
    }

    static void testCharBufferView() {
        CharSequence cs = java.nio.CharBuffer.wrap("hello");
        check(cs.charAt(2) == 'l', "CharBuffer charAt");
        check(cs.subSequence(1, 3).toString().equals("el"), "CharBuffer subSequence");
        check(((java.nio.CharBuffer) cs).position() == 0, "charAt does not move buffer position");
        CharSequence sup = java.nio.CharBuffer.wrap(new String(new int[]{0x1D11E, 'a'}, 0, 2));
        check(sup.length() == 3, "CharBuffer length counts UTF-16 units");
        check(sup.chars().toArray().length == 3, "CharBuffer chars() units");
        CharSequence empty = java.nio.CharBuffer.wrap("");
        check(empty.length() == 0, "empty CharBuffer");
        check(java.nio.CharBuffer.wrap("ab").chars().sum() == 'a' + 'b', "CharBuffer chars() sum");
    }

    static void testSubSequenceWindows() {
        Seq seq = new Seq("abcdef");
        check(seq.subSequence(0, 0).toString().isEmpty(), "window (0,0)");
        check(seq.subSequence(0, 1).toString().equals("a"), "window head");
        check(seq.subSequence(5, 6).toString().equals("f"), "window tail");
        check(seq.subSequence(0, 6).toString().equals("abcdef"), "window full");
        check(seq.subSequence(0, 6).length() == seq.length(), "full window length equals length()");
        // 接口不变量：isEmpty() 语义（8 侧无 isEmpty 默认方法，用 length()==0 表达）。
        check(new Seq("").length() == 0, "empty impl expressed as length()==0 (JDK-8 has no isEmpty default)");
        check(new Seq("x").length() != 0, "non-empty impl length()!=0");
    }

    public static void main(String[] args) {
        testStringAsCharSequence();
        testCharAtBounds();
        testSubSequenceBounds();
        testStreams();
        testCustomImplContract();
        testPolymorphicImpls();
        testDefaultStreamRouting();
        testImplBounds();
        testCharBufferView();
        testSubSequenceWindows();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
