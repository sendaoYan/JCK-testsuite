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

    static final class CS implements CharSequence {
        final String s;
        CS(String s) { this.s = s; }
        public int length() { return s.length(); }
        public char charAt(int i) { return s.charAt(i); }
        public CharSequence subSequence(int a, int b) { return s.substring(a, b); }
        public String toString() { return s; }
    }

    static void testIsEmpty() {
        check("".isEmpty(), "String empty isEmpty");
        check(!"x".isEmpty(), "String non-empty !isEmpty");
        CharSequence cs = new CS("ab");
        check(!cs.isEmpty(), "CS non-empty !isEmpty");
        check(new CS("").isEmpty(), "CS empty isEmpty (default method)");
    }

    static void testCompare() {
        check(CharSequence.compare("abc", "abc") == 0, "compare equal");
        check(CharSequence.compare("abc", "abd") < 0, "compare <");
        check(CharSequence.compare("abd", "abc") > 0, "compare >");
        check(CharSequence.compare("ab", "abc") < 0, "compare shorter prefix");
        check(CharSequence.compare("abc", "ab") > 0, "compare longer");
        check(CharSequence.compare(new CS("a"), new CS("b")) < 0, "compare custom CS");
        check(CharSequence.compare("", "") == 0, "compare both empty");
        try {
            CharSequence.compare(null, "a");
            throw new AssertionError("compare(null,...): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testGetChars() {
        CS cs = new CS("hello");
        char[] dst = new char[5];
        cs.getChars(0, 5, dst, 0);
        check(new String(dst).equals("hello"), "default getChars(int,int,char[],int)");
        char[] dst2 = new char[6];
        cs.getChars(1, 4, dst2, 1);
        check(dst2[0] == 0 && new String(dst2, 1, 3).equals("ell"), "getChars into offset");
        "hello".getChars(0, 5, dst, 0);
        check(new String(dst).equals("hello"), "String getChars(int,int,char[],int)");
        try {
            cs.getChars(1, 6, dst2, 0);
            throw new AssertionError("getChars overflow: expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
    }

    // ===================== 契约深化（2026-11）追加 =====================
    // 自定义实现 + 四实现多态一致性 + 默认流方法经 charAt 路由实证 +
    // 静态 compare 精确矩阵 + isEmpty/getChars 默认方法矩阵。

    /** 统计 charAt 调用次数，证明默认流/拷贝方法逐索引路由到 charAt。 */
    static final class CountingCS implements CharSequence {
        final String s;
        int charAtCalls;
        CountingCS(String s) { this.s = s; }
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
        CS seq = new CS("hello");
        check(seq.length() == 5, "CS length");
        check(seq.charAt(0) == 'h' && seq.charAt(4) == 'o', "CS charAt endpoints");
        check(seq.subSequence(1, 3).toString().equals("el"), "CS subSequence(1,3)");
        check(seq.toString().equals("hello"), "CS toString");
        check(seq.subSequence(0, 5).toString().equals("hello"), "CS subSequence full window");
        check(seq.subSequence(2, 2).toString().isEmpty(), "CS subSequence(2,2) empty");
        CharSequence inner = seq.subSequence(1, 4);
        check(inner.subSequence(0, 2).toString().equals("el"), "chained subSequence");
        check(inner.length() == 3, "subSequence length");
        check(inner.charAt(2) == 'l', "subSequence charAt relative index");
        CS empty = new CS("");
        check(empty.length() == 0 && empty.toString().isEmpty(), "empty CS");
        CS one = new CS("x");
        check(one.charAt(0) == 'x' && one.subSequence(0, 1).toString().equals("x"), "single-char CS");
        // subSequence 结果仍是 CharSequence，可用全部默认方法。
        check(inner.isEmpty() == false && new CS("").subSequence(0, 0).isEmpty(),
              "isEmpty applies to subSequence results");
    }

    static void testPolymorphicImpls() {
        CharSequence[] impls = {
            "hello",
            new StringBuilder("hello"),
            new StringBuffer("hello"),
            java.nio.CharBuffer.wrap("hello"),
            new CS("hello")
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
            check(cs.isEmpty() == (cs.length() == 0), cs.getClass().getSimpleName() + " isEmpty invariant");
        }
        CharSequence s = "hello";
        check(s.subSequence(0, 5) == s, "String subSequence(0,len) returns this via interface");
    }

    static void testDefaultStreamRouting() {
        CountingCS c = new CountingCS("abc");
        int[] arr = c.chars().toArray();
        check(arr.length == 3 && arr[0] == 'a' && arr[1] == 'b' && arr[2] == 'c',
              "default chars() toArray exact");
        check(c.charAtCalls == 3, "default chars() calls charAt once per index (locked)");
        CountingCS c2 = new CountingCS("abc");
        check(c2.chars().sum() == 'a' + 'b' + 'c', "default chars() sum");
        String sup = new String(new int[]{0x1D11E, 'a'}, 0, 2);
        CountingCS cs = new CountingCS(sup);
        int[] units = cs.chars().toArray();
        check(units.length == 3 && units[0] == 0xD834 && units[1] == 0xDD1E && units[2] == 'a',
              "default chars() yields UTF-16 units over surrogate pair");
        check(cs.charAtCalls == 3, "chars() over pair: charAt called for each UTF-16 unit");
        CountingCS cp = new CountingCS(sup);
        int[] cps = cp.codePoints().toArray();
        check(cps.length == 2 && cps[0] == 0x1D11E && cps[1] == 'a',
              "default codePoints() decodes surrogate pairs");
        check(cp.charAtCalls == 3, "codePoints() calls charAt once per UTF-16 unit");
        CountingCS blank = new CountingCS("");
        check(blank.chars().count() == 0 && blank.codePoints().count() == 0,
              "empty CS streams count 0");
        CharSequence str = "ab";
        check(str.chars().sum() == 'a' + 'b', "String chars() via CharSequence reference");
        check(new StringBuilder("ab").chars().count() == 2, "StringBuilder chars() count");
    }

    static void testImplBounds() {
        CS seq = new CS("abc");
        expectIOOBE(() -> seq.charAt(-1), "CS charAt(-1)");
        expectIOOBE(() -> seq.charAt(3), "CS charAt(len)");
        expectIOOBE(() -> seq.subSequence(-1, 2), "CS subSequence(-1,2)");
        expectIOOBE(() -> seq.subSequence(1, 4), "CS subSequence(1,4)");
        expectIOOBE(() -> seq.subSequence(2, 1), "CS subSequence(2,1)");
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
        check(empty.length() == 0 && empty.isEmpty(), "empty CharBuffer + isEmpty");
        check(java.nio.CharBuffer.wrap("ab").chars().sum() == 'a' + 'b', "CharBuffer chars() sum");
    }

    static void testCompareMatrix() {
        check(CharSequence.compare("abc", "abc") == 0, "compare equal");
        check(CharSequence.compare("a", "c") == -2, "compare exact char diff -2");
        check(CharSequence.compare("ab", "abc") == -1, "compare length delta -1");
        check(CharSequence.compare("abc", "ab") == 1, "compare length delta +1");
        check(CharSequence.compare("a", "") == 1, "compare vs empty");
        check(CharSequence.compare("", "a") == -1, "compare empty vs");
        check(CharSequence.compare(new StringBuilder("hello"), "hello") == 0,
              "compare cross-type StringBuilder vs String");
        check(CharSequence.compare("hello", new StringBuffer("hello")) == 0,
              "compare String vs StringBuffer");
        check(CharSequence.compare(new CS("hello"), new CS("hello")) == 0,
              "compare custom impls");
        check(CharSequence.compare("x", "y") == -CharSequence.compare("y", "x"),
              "compare antisymmetry exact");
        check(CharSequence.compare("HELLO", "hello") == 'H' - 'h',
              "compare case-sensitive exact diff");
        try {
            CharSequence.compare("a", null);
            throw new AssertionError("compare(a,null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        // 高 BMP 与代理区：按 char 单元比较。
        check(CharSequence.compare("Ａ", "Ｂ") == -1, "compare high BMP neighbors");
    }

    static void testIsEmptyDefaultImpls() {
        check(new CS("").isEmpty(), "custom empty isEmpty (default method)");
        check(!new CS("x").isEmpty(), "custom non-empty");
        check(java.nio.CharBuffer.wrap("").isEmpty(), "CharBuffer empty");
        check(!java.nio.CharBuffer.wrap("x").isEmpty(), "CharBuffer non-empty");
        check(!((CharSequence) new StringBuilder("x")).isEmpty(), "StringBuilder via interface");
        check(((CharSequence) new StringBuilder()).isEmpty(), "empty StringBuilder via interface");
        check(new CS("  ").isEmpty() == false, "blank-but-not-empty is not empty");
    }

    static void testGetCharsMatrix() {
        CS cs = new CS("hello");
        char[] dst = new char[5];
        cs.getChars(0, 5, dst, 0);
        check(new String(dst).equals("hello"), "default getChars full window");
        char[] partial = new char[3];
        cs.getChars(1, 4, partial, 0);
        check(new String(partial).equals("ell"), "default getChars partial window");
        char[] offset = new char[4];
        cs.getChars(0, 3, offset, 1);
        check(offset[0] == 0 && new String(offset, 1, 3).equals("hel"), "default getChars dst offset");
        char[] none = new char[1];
        none[0] = 'q';
        cs.getChars(2, 2, none, 0);
        check(none[0] == 'q', "empty window copies nothing");
        char[] viaString = new char[5];
        "hello".getChars(0, 5, viaString, 0);
        check(java.util.Arrays.equals(viaString, dst), "String getChars matches default result");
        CountingCS cc = new CountingCS("hello");
        cc.getChars(1, 4, new char[3], 0);
        check(cc.charAtCalls == 3, "default getChars routes through charAt per index");
        expectIOOBE(() -> cs.getChars(-1, 2, new char[5], 0), "getChars negative begin");
        expectIOOBE(() -> cs.getChars(0, 6, new char[5], 0), "getChars end overflow");
        expectIOOBE(() -> cs.getChars(3, 1, new char[5], 0), "getChars begin>end");
        expectIOOBE(() -> cs.getChars(0, 5, new char[2], 0), "getChars dst too small");
        expectIOOBE(() -> cs.getChars(0, 5, new char[5], 1), "getChars dst offset overflow");
    }

    // ---------- white-box: structural / reflection ----------
    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try {
            c.getMethod(name, params);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.CharSequence");
        check(c.isInterface(), "CharSequence is an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "CharSequence abstract");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "CharSequence not final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasMethod(c, "length"), "abstract length()");
        check(hasMethod(c, "charAt", int.class), "abstract charAt(int)");
        check(hasMethod(c, "subSequence", int.class, int.class), "abstract subSequence(int,int)");
        check(hasMethod(c, "toString"), "abstract toString()");
        check(hasMethod(c, "isEmpty"), "default isEmpty()");
        check(hasMethod(c, "chars"), "default chars()");
        check(hasMethod(c, "codePoints"), "default codePoints()");
        check(hasMethod(c, "getChars", int.class, int.class, char[].class, int.class),
              "default getChars(int,int,char[],int)");
        check(hasMethod(c, "compare", CharSequence.class, CharSequence.class),
              "static compare(CharSequence,CharSequence)");
        check(java.lang.reflect.Modifier.isStatic(
                  c.getMethod("compare", CharSequence.class, CharSequence.class).getModifiers()),
              "compare is static");
        check(c.getMethod("isEmpty").isDefault(), "isEmpty is default");
    }

    // ---------- black-box: behavioral composition ----------
    static void testBlackBox() {
        testStringAsCharSequence();
        testCharAtBounds();
        testSubSequenceBounds();
        testStreams();
        testIsEmpty();
        testCompare();
        testGetChars();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // 契约深化（2026-11）追加
        testCustomImplContract();
        testPolymorphicImpls();
        testDefaultStreamRouting();
        testImplBounds();
        testCharBufferView();
        testCompareMatrix();
        testIsEmptyDefaultImpls();
        testGetCharsMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
