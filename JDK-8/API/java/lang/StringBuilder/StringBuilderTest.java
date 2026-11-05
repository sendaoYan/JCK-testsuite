/*
 * @test
 * @summary java.lang.StringBuilder API conformance: constructors + capacity +
 *          negative-capacity/null boundary exceptions, append overloads +
 *          appendCodePoint (chaining returns this + supplementary), null-argument
 *          semantics matrix, length/charAt/setCharAt/setLength/getChars,
 *          insert/delete/deleteCharAt/replace, reverse/substring/subSequence/indexOf
 *          (incl. fromIndex), code points, ensureCapacity/trimToSize, streams, and
 *          the structural contract (final, extends AbstractStringBuilder, implements
 *          Appendable/CharSequence/Serializable, not synchronized).
 * @run main StringBuilderTest
 */

public class StringBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectSIOOBE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException expected) {
            passed++;
        }
    }

    static void expectIOOBE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
    }

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void expectIAE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    static void testConstructors() {
        StringBuilder a = new StringBuilder();
        check(a.capacity() == 16 && a.length() == 0, "default ctor capacity 16");
        check(new StringBuilder("abc").capacity() == 19, "String ctor capacity 16+3");
        check(new StringBuilder("abc").toString().equals("abc"), "String ctor content");
        check(new StringBuilder(50).capacity() == 50, "int ctor capacity");
        CharSequence cs = "hello";
        check(new StringBuilder(cs).toString().equals("hello"), "CharSequence ctor");
        // capacity 0 is legal (grows on first append).
        StringBuilder z = new StringBuilder(0);
        check(z.capacity() == 0 && z.length() == 0, "capacity 0 legal");
        z.append('x');
        check(z.toString().equals("x") && z.capacity() >= 1, "capacity 0 grows on append");
    }

    static void testConstructorExceptions() {
        try {
            new StringBuilder(-1);
            throw new AssertionError("new StringBuilder(-1): expected NegativeArraySizeException");
        } catch (NegativeArraySizeException expected) {
            passed++;
        }
        expectNPE(() -> new StringBuilder((String) null), "StringBuilder((String)null)");
        expectNPE(() -> new StringBuilder((CharSequence) null), "StringBuilder((CharSequence)null)");
    }

    static void testAppend() {
        StringBuilder sb = new StringBuilder();
        check(sb.append(true) == sb, "append returns this");
        check(sb.toString().equals("true"), "append(boolean)");

        sb.setLength(0);
        sb.append('a').append("bc").append(123).append(4.5).append(9L);
        check(sb.toString().equals("abc1234.59"), "append mixed primitives");

        sb.setLength(0);
        char[] ca = {'x', 'y', 'z'};
        sb.append(ca);
        check(sb.toString().equals("xyz"), "append(char[])");
        sb.append(ca, 1, 2);
        check(sb.toString().equals("xyzyz"), "append(char[],off,len)");

        sb.setLength(0);
        CharSequence cs = "hello";
        sb.append(cs);
        check(sb.toString().equals("hello"), "append(CharSequence)");
        sb.append(cs, 1, 3);
        check(sb.toString().equals("helloel"), "append(CharSequence,start,end)");

        sb.setLength(0);
        sb.append(Integer.valueOf(42));
        check(sb.toString().equals("42"), "append(Object)");
    }

    static void testAppendNullSemantics() {
        // append((String)null), append((Object)null), append((CharSequence)null) -> "null".
        check(new StringBuilder().append((String) null).toString().equals("null"),
              "append((String)null) == \"null\"");
        check(new StringBuilder().append((Object) null).toString().equals("null"),
              "append((Object)null) == \"null\"");
        check(new StringBuilder().append((CharSequence) null).toString().equals("null"),
              "append((CharSequence)null) == \"null\"");
        // append((char[])null) -> NPE (not "null").
        expectNPE(() -> new StringBuilder().append((char[]) null), "append((char[])null)");
        expectNPE(() -> new StringBuilder().append((char[]) null, 0, 0),
                  "append((char[])null,off,len)");
        // append((CharSequence)null, start, end) coerces null to "null" then slices.
        check(new StringBuilder().append((CharSequence) null, 0, 4).toString().equals("null"),
              "append((CharSequence)null,0,4) == \"null\"");
        expectIOOBE(() -> new StringBuilder().append((CharSequence) null, 0, 5),
                    "append((CharSequence)null,0,5) past \"null\" length");
        expectIOOBE(() -> new StringBuilder().append(new char[]{'a'}, -1, 1),
                    "append(char[],-1,1)");
        expectIOOBE(() -> new StringBuilder().append(new char[]{'a'}, 0, 2),
                    "append(char[],0,2)");
        expectIOOBE(() -> new StringBuilder().append("ab", -1, 1),
                    "append(CharSequence,-1,1)");
        expectIOOBE(() -> new StringBuilder().append("ab", 0, 3),
                    "append(CharSequence,0,3)");
    }

    static void testAppendCodePoint() {
        StringBuilder sb = new StringBuilder();
        sb.appendCodePoint(0x1D518);
        check(sb.length() == 2, "appendCodePoint length +2");
        check(sb.codePointAt(0) == 0x1D518, "appendCodePoint value");
        check(sb.codePointCount(0, 2) == 1, "codePointCount");
        check(sb.codePointBefore(2) == 0x1D518, "codePointBefore");
        check(sb.offsetByCodePoints(0, 1) == 2, "offsetByCodePoints");

        StringBuilder bmp = new StringBuilder();
        bmp.appendCodePoint('A');
        check(bmp.length() == 1 && bmp.charAt(0) == 'A', "appendCodePoint BMP");

        expectIAE(() -> new StringBuilder().appendCodePoint(Character.MIN_CODE_POINT - 1),
                  "appendCodePoint(negative)");
        expectIAE(() -> new StringBuilder().appendCodePoint(Character.MAX_CODE_POINT + 1),
                  "appendCodePoint(> MAX_CODE_POINT)");
    }

    static void testLengthChars() {
        StringBuilder sb = new StringBuilder("abc");
        check(sb.length() == 3 && sb.charAt(1) == 'b', "length/charAt");
        sb.setCharAt(1, 'X');
        check(sb.toString().equals("aXc"), "setCharAt");
        sb.setLength(2);
        check(sb.toString().equals("aX"), "setLength truncate");
        sb.setLength(5);
        check(sb.length() == 5 && sb.charAt(3) == '\0', "setLength pads with NUL");

        char[] dst = new char[3];
        new StringBuilder("abc").getChars(0, 3, dst, 0);
        check(dst[0] == 'a' && dst[2] == 'c', "getChars");
        expectSIOOBE(() -> sb.charAt(-1), "charAt(-1)");
        expectSIOOBE(() -> sb.charAt(5), "charAt(len)");
    }

    static void testExceptionMatrix() {
        StringBuilder sb = new StringBuilder("abc");
        expectSIOOBE(() -> sb.setCharAt(-1, 'x'), "setCharAt(-1)");
        expectSIOOBE(() -> sb.setCharAt(3, 'x'), "setCharAt(len)");
        expectSIOOBE(() -> sb.getChars(-1, 3, new char[3], 0), "getChars(-1,3)");
        expectSIOOBE(() -> sb.getChars(0, 4, new char[3], 0), "getChars(0,4)");
        expectSIOOBE(() -> sb.getChars(2, 1, new char[3], 0), "getChars(2,1)");
        expectIOOBE(() -> sb.getChars(0, 3, new char[2], 0), "getChars dest too small");
        expectSIOOBE(() -> sb.substring(-1), "substring(-1)");
        expectSIOOBE(() -> sb.substring(4), "substring(len+1)");
        expectSIOOBE(() -> sb.substring(2, 1), "substring(begin>end)");
        expectSIOOBE(() -> sb.subSequence(-1, 1), "subSequence(-1,1)");

        StringBuilder d = new StringBuilder("abcdef");
        expectSIOOBE(() -> d.delete(-1, 1), "delete(-1,1)");
        expectSIOOBE(() -> d.delete(2, 1), "delete(begin>end)");
        expectSIOOBE(() -> d.deleteCharAt(-1), "deleteCharAt(-1)");
        expectSIOOBE(() -> d.deleteCharAt(6), "deleteCharAt(len)");
        expectSIOOBE(() -> d.replace(-1, 1, "x"), "replace(-1,1)");
        expectSIOOBE(() -> d.replace(7, 1, "x"), "replace(start>end after clamp)");
        // delete/replace clamp end to length (not an error).
        check(new StringBuilder("abcdef").delete(0, 7).toString().equals(""),
              "delete(0,len+1) clamps end to empty");
        check(new StringBuilder("abcdef").replace(0, 7, "x").toString().equals("x"),
              "replace(0,len+1) clamps end");

        expectNPE(() -> sb.indexOf((String) null), "indexOf((String)null)");
        expectNPE(() -> sb.indexOf((String) null, 0), "indexOf((String)null,0)");
        expectNPE(() -> sb.lastIndexOf((String) null), "lastIndexOf((String)null)");
        expectNPE(() -> sb.lastIndexOf((String) null, 0), "lastIndexOf((String)null,0)");
        expectNPE(() -> sb.insert(0, (char[]) null), "insert((char[])null)");
        expectNPE(() -> sb.insert(0, (char[]) null, 0, 0), "insert((char[])null,off,len)");
    }

    static void testInsertDelete() {
        StringBuilder sb = new StringBuilder("abcd");
        sb.insert(2, "XY");
        check(sb.toString().equals("abXYcd"), "insert String");
        sb.insert(0, true);
        check(sb.toString().equals("trueabXYcd"), "insert boolean");
        sb.insert(sb.length(), '!');
        check(sb.toString().equals("trueabXYcd!"), "insert char at end");

        StringBuilder d = new StringBuilder("abcdef");
        d.delete(1, 3);
        check(d.toString().equals("adef"), "delete(1,3)");
        d.deleteCharAt(0);
        check(d.toString().equals("def"), "deleteCharAt(0)");
        d.replace(1, 3, "XY");
        check(d.toString().equals("dXY"), "replace(1,3,XY)");
        // JDK-8 throws ArrayIndexOutOfBoundsException here, JDK-27 throws
        // StringIndexOutOfBoundsException; both are IndexOutOfBoundsException.
        expectIOOBE(() -> new StringBuilder("ab").insert(5, 'x'), "insert past end");
    }

    static void testInsertOverloads() {
        StringBuilder sb = new StringBuilder("x");
        sb.insert(1, new char[]{'a', 'b'});
        check(sb.toString().equals("xab"), "insert char[]");
        sb.setLength(1);
        sb.insert(1, 2.5);
        check(sb.toString().equals("x2.5"), "insert double");
        sb.setLength(1);
        sb.insert(1, 99L);
        check(sb.toString().equals("x99"), "insert long");
        sb.setLength(1);
        sb.insert(1, 3.14f);
        check(sb.toString().equals("x3.14"), "insert float");
        sb.setLength(1);
        sb.insert(1, Integer.valueOf(7));
        check(sb.toString().equals("x7"), "insert Object");
        // insert null String/Object/CharSequence -> "null".
        check(new StringBuilder().insert(0, (String) null).toString().equals("null"),
              "insert((String)null) == \"null\"");
        check(new StringBuilder().insert(0, (Object) null).toString().equals("null"),
              "insert((Object)null) == \"null\"");
        check(new StringBuilder().insert(0, (CharSequence) null).toString().equals("null"),
              "insert((CharSequence)null) == \"null\"");
    }

    static void testReverseSubstringIndex() {
        StringBuilder sb = new StringBuilder("abc");
        check(sb.reverse().toString().equals("cba"), "reverse");
        check(sb.substring(1).equals("ba"), "substring(1)");
        check(sb.substring(1, 2).equals("b"), "substring(1,2)");
        check(sb.subSequence(0, 2).equals("cb"), "subSequence");
        check(sb.indexOf("b") == 1, "indexOf");
        check(sb.lastIndexOf("b") == 1, "lastIndexOf");
        check(sb.indexOf("z") == -1, "indexOf missing");
    }

    static void testIndexOfFromIndex() {
        StringBuilder sb = new StringBuilder("banana");
        check(sb.indexOf("an", 1) == 1, "indexOf(str,1)");
        check(sb.indexOf("an", 2) == 3, "indexOf(str,2)");
        check(sb.indexOf("zz", 0) == -1, "indexOf missing");
        check(sb.lastIndexOf("an", 5) == 3, "lastIndexOf(str,5)");
        check(sb.lastIndexOf("an", 2) == 1, "lastIndexOf(str,2)");
    }

    static void testCapacity() {
        StringBuilder sb = new StringBuilder(8);
        sb.ensureCapacity(32);
        check(sb.capacity() >= 32, "ensureCapacity grows");
        sb.append("hello");
        sb.trimToSize();
        check(sb.capacity() == 5, "trimToSize capacity == length");
    }

    static void testStreams() {
        StringBuilder sb = new StringBuilder("ab");
        check(sb.chars().count() == 2, "chars count");
        check(sb.codePoints().count() == 2, "codePoints count");
        check(sb.chars().sum() == 'a' + 'b', "chars sum");
    }

    static boolean isSynchronized(Class<?> c, String name, Class<?>... params) throws Exception {
        return java.lang.reflect.Modifier.isSynchronized(
            c.getMethod(name, params).getModifiers());
    }

    static void testNotSynchronized() throws Exception {
        check(!isSynchronized(StringBuilder.class, "append", String.class),
              "StringBuilder.append not synchronized");
        check(!isSynchronized(StringBuilder.class, "reverse"),
              "StringBuilder.reverse not synchronized");
        check(!isSynchronized(StringBuilder.class, "toString"),
              "StringBuilder.toString not synchronized");
    }

    static void testBoundaryEquivalence() {
        // reverse is an involution.
        StringBuilder sb = new StringBuilder("stressed");
        sb.reverse();
        check(sb.toString().equals("desserts"), "reverse('stressed')");
        sb.reverse();
        check(sb.toString().equals("stressed"), "reverse involution");

        // setLength(0) empties without changing capacity.
        StringBuilder c = new StringBuilder("abcdef");
        int cap = c.capacity();
        c.setLength(0);
        check(c.length() == 0 && c.capacity() == cap, "setLength(0) preserves capacity");

        // insert at boundaries 0 and length.
        StringBuilder ins = new StringBuilder("ab");
        ins.insert(0, 'Z');
        check(ins.toString().equals("Zab"), "insert char at 0");
        ins.insert(ins.length(), '!');
        check(ins.toString().equals("Zab!"), "insert char at length");

        // delete(0,0) is a no-op.
        StringBuilder d = new StringBuilder("abc");
        d.delete(0, 0);
        check(d.toString().equals("abc"), "delete(0,0) no-op");

        // capacity auto-grows beyond the initial 16.
        StringBuilder g = new StringBuilder();
        for (int i = 0; i < 100; i++) g.append('x');
        check(g.length() == 100 && g.capacity() >= 100, "auto-grow capacity >= length");

        // toString -> new StringBuilder(String) round-trip.
        check(new StringBuilder(g.toString()).toString().equals(g.toString()), "toString round-trip");
    }

    // ---------- white-box: structural / reflection ----------
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) {
            if (i.getName().equals(name)) return true;
        }
        return false;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try {
            c.getMethod(name, params);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.StringBuilder");
        check(!c.isInterface(), "StringBuilder not an interface");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "StringBuilder is final");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "StringBuilder not abstract");
        check(c.getSuperclass().getName().equals("java.lang.AbstractStringBuilder"),
              "extends AbstractStringBuilder");
        check(java.lang.Appendable.class.isAssignableFrom(c), "implements Appendable");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(hasInterface(c, "java.lang.CharSequence"), "implements CharSequence");
        check(hasMethod(c, "append", String.class), "method append(String)");
        check(hasMethod(c, "reverse"), "method reverse()");
    }

    // ===================== contract deepening (2026-11) =====================
    // Version-adaptive constants: differences between JDK 8 and JDK 27 (exception
    // classes and exact messages) are encoded here once and asserted per side.

    static final boolean MODERN = !System.getProperty("java.version").startsWith("1.");
    static final String CLASS_SIOOBE = "java.lang.StringIndexOutOfBoundsException";
    static final String CLASS_IOOBE = "java.lang.IndexOutOfBoundsException";
    static final String CLASS_AIOOBE = "java.lang.ArrayIndexOutOfBoundsException";

    /** Exact-class + exact-message catcher: 2 assertions per successful call. */
    static void expectExact(Runnable r, String className, String message, String label) {
        try {
            r.run();
            throw new AssertionError(label + ": expected " + className + " but no exception was thrown");
        } catch (AssertionError up) {
            throw up;
        } catch (Throwable t) {
            check(t.getClass().getName().equals(className),
                  label + " type: expected " + className + " but got " + t.getClass().getName());
            check(message == null ? t.getMessage() == null : message.equals(t.getMessage()),
                  label + " message: expected \"" + message + "\" but got \"" + t.getMessage() + "\"");
        }
    }

    /** Class-only catcher (used where the message text differs per release): 1 assertion. */
    static void expectClass(Runnable r, String className, String label) {
        try {
            r.run();
            throw new AssertionError(label + ": expected " + className + " but no exception was thrown");
        } catch (AssertionError up) {
            throw up;
        } catch (Throwable t) {
            check(t.getClass().getName().equals(className),
                  label + " type: expected " + className + " but got " + t.getClass().getName());
        }
    }

    /** Minimal user CharSequence so append/insert polymorphism runs off the JDK path. */
    static class WrapCs implements CharSequence {
        private final String s;
        WrapCs(String s) { this.s = s; }
        public int length() { return s.length(); }
        public char charAt(int index) { return s.charAt(index); }
        public CharSequence subSequence(int start, int end) { return s.subSequence(start, end); }
        public String toString() { return s; }
    }

    static void testAppendPrimitivesExact() {
        check(new StringBuilder().append(false).toString().equals("false"), "append(false)");
        check(new StringBuilder().append('Z').toString().equals("Z"), "append(char) exact");
        check(new StringBuilder().append(0).toString().equals("0"), "append(0)");
        check(new StringBuilder().append(-1).toString().equals("-1"), "append(-1)");
        check(new StringBuilder().append(Integer.MIN_VALUE).toString().equals("-2147483648"), "append(Integer.MIN_VALUE)");
        check(new StringBuilder().append(Integer.MAX_VALUE).toString().equals("2147483647"), "append(Integer.MAX_VALUE)");
        check(new StringBuilder().append(0L).toString().equals("0"), "append(0L)");
        check(new StringBuilder().append(Long.MIN_VALUE).toString().equals("-9223372036854775808"), "append(Long.MIN_VALUE)");
        check(new StringBuilder().append(Long.MAX_VALUE).toString().equals("9223372036854775807"), "append(Long.MAX_VALUE)");
        check(new StringBuilder().append(0.0f).toString().equals("0.0"), "append(0.0f)");
        check(new StringBuilder().append(1.5f).toString().equals("1.5"), "append(1.5f)");
        check(new StringBuilder().append(-2.5f).toString().equals("-2.5"), "append(-2.5f)");
        check(new StringBuilder().append(0.1f).toString().equals("0.1"), "append(0.1f)");
        check(new StringBuilder().append(0.5).toString().equals("0.5"), "append(0.5)");
        check(new StringBuilder().append(-0.25).toString().equals("-0.25"), "append(-0.25)");
        check(new StringBuilder().append(1.0E-5).toString().equals("1.0E-5"), "append(1.0E-5)");
        check(new StringBuilder().append(100.0).toString().equals("100.0"), "append(100.0)");
        StringBuilder keep = new StringBuilder("ab");
        keep.append("");
        check(keep.length() == 2 && keep.toString().equals("ab"), "append(\"\") keeps content");
        StringBuilder th = new StringBuilder();
        check(th.append(true) == th && th.append('c') == th && th.append(1) == th
              && th.append(1L) == th && th.append(1f) == th && th.append(1d) == th
              && th.append(new char[]{'x'}) == th && th.append("s") == th
              && th.append((CharSequence) "cs") == th, "every append overload returns this");
        check(th.toString().equals("truec111.01.0xscs"), "chained appends exact");
    }

    static void testAppendCharSequenceSources() {
        // CharSequence dispatch from String / StringBuilder / StringBuffer / a custom impl.
        check(new StringBuilder().append((CharSequence) "world").toString().equals("world"), "append(CharSequence from String)");
        check(new StringBuilder().append((CharSequence) new StringBuilder("bld")).toString().equals("bld"), "append(CharSequence from StringBuilder)");
        check(new StringBuilder().append((CharSequence) new StringBuffer("buf")).toString().equals("buf"), "append(CharSequence from StringBuffer)");
        check(new StringBuilder().append((CharSequence) new WrapCs("wrap")).toString().equals("wrap"), "append(custom CharSequence)");
        // (start, end) slices from each source kind
        check(new StringBuilder().append((CharSequence) "hello", 1, 3).toString().equals("el"), "slice from String");
        check(new StringBuilder().append((CharSequence) new StringBuilder("hello"), 1, 3).toString().equals("el"), "slice from StringBuilder");
        check(new StringBuilder().append((CharSequence) new StringBuffer("hello"), 1, 3).toString().equals("el"), "slice from StringBuffer");
        check(new StringBuilder().append((CharSequence) new WrapCs("hello"), 1, 3).toString().equals("el"), "slice from custom CharSequence");
        check(new StringBuilder().append((CharSequence) "hello", 2, 2).toString().equals(""), "empty slice");
        check(new StringBuilder().append((CharSequence) "hello", 0, 5).toString().equals("hello"), "full-range slice");
        // null CharSequence with (start, end): spec treats null as "null" and slices that literal.
        check(new StringBuilder().append((CharSequence) null, 1, 4).toString().equals("ull"), "append(null CS,1,4) slices \"null\"");
        check(new StringBuilder().append((CharSequence) null, 0, 4).toString().equals("null"), "append(null CS,0,4) whole \"null\"");
        check(new StringBuilder().append((CharSequence) null, 4, 4).toString().equals(""), "append(null CS,4,4) empty slice");
        check(new StringBuilder().append((CharSequence) null, 0, 0).toString().equals(""), "append(null CS,0,0) empty slice");
    }

    static void testAppendSelfAndCross() {
        StringBuilder one = new StringBuilder("a");
        one.append(one);
        check(one.length() == 2 && one.toString().equals("aa"), "self-append \"a\" -> \"aa\"");
        StringBuilder two = new StringBuilder("ab");
        two.append(two);
        check(two.length() == 4 && two.toString().equals("abab"), "self-append \"ab\" -> \"abab\"");
        check(new StringBuilder("x").append(new StringBuffer("y")).toString().equals("xy"), "append(StringBuffer) cross-type");
        check(new StringBuilder("x").append((CharSequence) new StringBuilder("z")).toString().equals("xz"), "append(StringBuilder) via CharSequence");
        check(new StringBuilder("x").append((CharSequence) new StringBuffer("y")).toString().equals("xy"), "append(StringBuffer) via CharSequence");
        check(new StringBuilder("x").append((StringBuffer) null).toString().equals("xnull"), "append((StringBuffer)null) appends \"null\"");
    }

    static void testAppendCodePointEdges() {
        StringBuilder nul = new StringBuilder();
        check(nul.appendCodePoint(0) == nul && nul.length() == 1 && nul.charAt(0) == '\0',
              "appendCodePoint(0) writes a single NUL char");
        StringBuilder max = new StringBuilder();
        max.appendCodePoint(0x10FFFF);
        check(max.length() == 2 && max.charAt(0) == '\udbff' && max.charAt(1) == '\udfff',
              "appendCodePoint(MAX_CODE_POINT) writes a surrogate pair");
        check(max.codePointAt(0) == 0x10FFFF, "MAX_CODE_POINT round-trips via codePointAt");
        StringBuilder bmp = new StringBuilder();
        bmp.appendCodePoint(0xFFFF);
        check(bmp.length() == 1 && bmp.charAt(0) == '\uffff', "appendCodePoint(0xFFFF) writes one char");
        StringBuilder mus = new StringBuilder();
        mus.appendCodePoint(0x1D11E);
        check(mus.length() == 2 && mus.codePointCount(0, 2) == 1, "appendCodePoint(MUSICAL G CLEF) pair, +2 UTF-16 units");
        expectExact(() -> new StringBuilder().appendCodePoint(Character.MIN_CODE_POINT - 1),
              "java.lang.IllegalArgumentException",
              MODERN ? "Not a valid Unicode code point: 0xFFFFFFFF" : null,
              "appendCodePoint(-1)");
        expectExact(() -> new StringBuilder().appendCodePoint(Character.MAX_CODE_POINT + 1),
              "java.lang.IllegalArgumentException",
              MODERN ? "Not a valid Unicode code point: 0x110000" : null,
              "appendCodePoint(0x110000)");
    }

    static void testAppendCharArrayBoundsExact() {
        // JDK 8 surfaces these as raw ArrayIndexOutOfBoundsException (null message);
        // JDK 27 pre-checks and throws plain IndexOutOfBoundsException with a Range message.
        expectExact(() -> new StringBuilder().append(new char[]{'a'}, -1, 1),
              MODERN ? CLASS_IOOBE : CLASS_AIOOBE,
              MODERN ? "Range [-1, 0) out of bounds for length 1" : null,
              "append(char[],-1,1)");
        expectExact(() -> new StringBuilder().append(new char[]{'a'}, 0, 2),
              MODERN ? CLASS_IOOBE : CLASS_AIOOBE,
              MODERN ? "Range [0, 2) out of bounds for length 1" : null,
              "append(char[],0,2)");
        expectExact(() -> new StringBuilder().append(new char[]{'a'}, 1, -1),
              MODERN ? CLASS_IOOBE : CLASS_AIOOBE,
              MODERN ? "Range [1, 0) out of bounds for length 1" : null,
              "append(char[],1,-1)");
        expectExact(() -> new StringBuilder().append((CharSequence) "ab", -1, 1),
              CLASS_IOOBE,
              MODERN ? "Range [-1, 1) out of bounds for length 2" : "start -1, end 1, s.length() 2",
              "append(CharSequence,-1,1)");
        expectExact(() -> new StringBuilder().append((CharSequence) null, 0, 5),
              CLASS_IOOBE,
              MODERN ? "Range [0, 5) out of bounds for length 4" : "start 0, end 5, s.length() 4",
              "append(null CharSequence,0,5)");
    }

    static void testInsertBoundariesAndTypes() {
        check(new StringBuilder("bc").insert(0, 'a').toString().equals("abc"), "insert(char) at 0");
        check(new StringBuilder("ac").insert(1, 'b').toString().equals("abc"), "insert(char) middle");
        check(new StringBuilder("ab").insert(2, 'c').toString().equals("abc"), "insert(char) at length");
        check(new StringBuilder("ac").insert(1, "b").toString().equals("abc"), "insert(String) middle");
        check(new StringBuilder("ac").insert(1, 5).toString().equals("a5c"), "insert(int) middle");
        check(new StringBuilder("ac").insert(1, 7L).toString().equals("a7c"), "insert(long) middle");
        check(new StringBuilder("ac").insert(1, 2.5f).toString().equals("a2.5c"), "insert(float) middle");
        check(new StringBuilder("ac").insert(1, 3.5).toString().equals("a3.5c"), "insert(double) middle");
        check(new StringBuilder("ac").insert(1, true).toString().equals("atruec"), "insert(boolean) middle");
        check(new StringBuilder("ac").insert(1, Integer.valueOf(9)).toString().equals("a9c"), "insert(Object) middle");
        check(new StringBuilder("ac").insert(1, new char[]{'X', 'Y'}).toString().equals("aXYc"), "insert(char[]) middle");
        check(new StringBuilder("ac").insert(1, (CharSequence) "XY").toString().equals("aXYc"), "insert(CharSequence) middle");
        check(new StringBuilder("ac").insert(1, new char[]{'X', 'Y', 'Z'}, 1, 2).toString().equals("aYZc"), "insert(char[],off,len) slice");
        check(new StringBuilder("ac").insert(1, (CharSequence) "XYZ", 1, 3).toString().equals("aYZc"), "insert(CharSequence,s,e) slice");
        check(new StringBuilder("ac").insert(1, (CharSequence) "XYZ", 2, 2).toString().equals("ac"), "insert(CharSequence,2,2) empty slice");
        StringBuilder th = new StringBuilder("ab");
        check(th.insert(1, "X") == th && th.insert(1, 'X') == th && th.insert(1, 5) == th
              && th.insert(1, 5L) == th && th.insert(1, 5f) == th && th.insert(1, 5d) == th
              && th.insert(1, true) == th && th.insert(1, (Object) null) == th
              && th.insert(1, new char[]{'c'}) == th && th.insert(1, (CharSequence) "cs") == th,
              "every insert overload returns this");
        // null argument forms insert the four characters "null"
        check(new StringBuilder("qw").insert(0, (String) null).toString().equals("nullqw"), "insert(0,(String)null)");
        check(new StringBuilder("qw").insert(1, (String) null).toString().equals("qnullw"), "insert(mid,(String)null)");
        check(new StringBuilder("qw").insert(2, (String) null).toString().equals("qwnull"), "insert(end,(String)null)");
        check(new StringBuilder("qw").insert(1, (Object) null).toString().equals("qnullw"), "insert(mid,(Object)null)");
        check(new StringBuilder("qw").insert(1, (CharSequence) null).toString().equals("qnullw"), "insert(mid,(CharSequence)null)");
        check(new StringBuilder("qz").insert(1, (CharSequence) null, 1, 3).toString().equals("qulz"), "insert(mid,null CS,1,3) inserts \"ul\"");
        check(new StringBuilder("qz").insert(2, (CharSequence) null, 0, 0).toString().equals("qz"), "insert(end,null CS,0,0) inserts nothing");
        // out-of-range: exact class and message locked per release
        expectExact(() -> new StringBuilder("ab").insert(-1, 'x'),
              MODERN ? CLASS_SIOOBE : CLASS_AIOOBE,
              MODERN ? "Range [-1, 2) out of bounds for length 2" : null,
              "insert(-1,char)");
        expectExact(() -> new StringBuilder("ab").insert(3, 'x'),
              MODERN ? CLASS_SIOOBE : CLASS_AIOOBE,
              MODERN ? "Range [3, 2) out of bounds for length 2" : null,
              "insert(3,char)");
        expectExact(() -> new StringBuilder("ab").insert(-1, "x"),
              CLASS_SIOOBE,
              MODERN ? "Range [-1, 2) out of bounds for length 2" : "String index out of range: -1",
              "insert(-1,String)");
        expectExact(() -> new StringBuilder("ab").insert(3, "x"),
              CLASS_SIOOBE,
              MODERN ? "Range [3, 2) out of bounds for length 2" : "String index out of range: 3",
              "insert(3,String)");
        expectExact(() -> new StringBuilder("ab").insert(3, 5),
              CLASS_SIOOBE,
              MODERN ? "Range [3, 2) out of bounds for length 2" : "String index out of range: 3",
              "insert(3,int)");
        expectExact(() -> new StringBuilder("ab").insert(3, 5L),
              CLASS_SIOOBE,
              MODERN ? "Range [3, 2) out of bounds for length 2" : "String index out of range: 3",
              "insert(3,long)");
        expectExact(() -> new StringBuilder("ab").insert(3, 5f),
              CLASS_SIOOBE,
              MODERN ? "Range [3, 2) out of bounds for length 2" : "String index out of range: 3",
              "insert(3,float)");
        expectExact(() -> new StringBuilder("ab").insert(3, 5d),
              CLASS_SIOOBE,
              MODERN ? "Range [3, 2) out of bounds for length 2" : "String index out of range: 3",
              "insert(3,double)");
        expectExact(() -> new StringBuilder("ab").insert(3, true),
              CLASS_SIOOBE,
              MODERN ? "Range [3, 2) out of bounds for length 2" : "String index out of range: 3",
              "insert(3,boolean)");
        expectExact(() -> new StringBuilder("ab").insert(3, new Object()),
              CLASS_SIOOBE,
              MODERN ? "Range [3, 2) out of bounds for length 2" : "String index out of range: 3",
              "insert(3,Object)");
        expectExact(() -> new StringBuilder("ab").insert(3, new char[]{'x'}),
              CLASS_SIOOBE,
              MODERN ? "Range [3, 2) out of bounds for length 2" : "String index out of range: 3",
              "insert(3,char[])");
        expectExact(() -> new StringBuilder("ab").insert(3, (CharSequence) "x"),
              CLASS_SIOOBE,
              MODERN ? "Range [3, 2) out of bounds for length 2" : "String index out of range: 3",
              "insert(3,CharSequence)");
        expectExact(() -> new StringBuilder("ab").insert(3, new char[]{'x'}, 0, 1),
              CLASS_SIOOBE,
              MODERN ? "Range [3, 2) out of bounds for length 2" : "String index out of range: 3",
              "insert(3,char[],0,1)");
        expectExact(() -> new StringBuilder("ab").insert(3, (CharSequence) "x", 0, 1),
              MODERN ? CLASS_SIOOBE : CLASS_IOOBE,
              MODERN ? "Range [3, 2) out of bounds for length 2" : "dstOffset 3",
              "insert(3,CharSequence,0,1)");
    }

    static void testDeleteMatrix() {
        check(new StringBuilder("abc").delete(0, 0).toString().equals("abc"), "delete(0,0) identity");
        check(new StringBuilder("abc").delete(3, 3).toString().equals("abc"), "delete(len,len) identity");
        check(new StringBuilder("abc").delete(0, 3).toString().equals(""), "delete(0,len) empties");
        check(new StringBuilder("abcdef").delete(0, 1).toString().equals("bcdef"), "delete head");
        check(new StringBuilder("abcdef").delete(5, 6).toString().equals("abcde"), "delete tail");
        check(new StringBuilder("abcdef").delete(2, 99).toString().equals("ab"), "delete clamps end past length");
        StringBuilder d = new StringBuilder("abc");
        check(d.delete(1, 2) == d && d.toString().equals("ac"), "delete(1,2) middle returns this");
        StringBuilder dc = new StringBuilder("abc");
        check(dc.deleteCharAt(0) == dc && dc.toString().equals("bc"), "deleteCharAt(0) returns this");
        check(new StringBuilder("abc").deleteCharAt(1).toString().equals("ac"), "deleteCharAt middle");
        check(new StringBuilder("abc").deleteCharAt(2).toString().equals("ab"), "deleteCharAt tail");
        expectExact(() -> new StringBuilder("abc").delete(-1, 2),
              CLASS_SIOOBE,
              MODERN ? "Range [-1, 2) out of bounds for length 3" : "String index out of range: -1",
              "delete(-1,2)");
        expectExact(() -> new StringBuilder("abc").delete(2, 1),
              CLASS_SIOOBE,
              MODERN ? "Range [2, 1) out of bounds for length 3" : null,
              "delete(2,1) begin>end");
        expectExact(() -> new StringBuilder("abc").deleteCharAt(-1),
              CLASS_SIOOBE,
              MODERN ? "Index -1 out of bounds for length 3" : "String index out of range: -1",
              "deleteCharAt(-1)");
        expectExact(() -> new StringBuilder("abc").deleteCharAt(3),
              CLASS_SIOOBE,
              MODERN ? "Index 3 out of bounds for length 3" : "String index out of range: 3",
              "deleteCharAt(len)");
    }

    static void testReplaceMatrix() {
        check(new StringBuilder("ac").replace(0, 0, "b").toString().equals("bac"), "replace(0,0,s) is a pure insert");
        check(new StringBuilder("ac").replace(2, 2, "b").toString().equals("acb"), "replace(len,len,s) appends");
        check(new StringBuilder("abcdef").replace(1, 3, "XY").toString().equals("aXYdef"), "replace equal length");
        check(new StringBuilder("abcdef").replace(1, 3, "XYZW").toString().equals("aXYZWdef"), "replace grows");
        check(new StringBuilder("abcdef").replace(1, 4, "Z").toString().equals("aZef"), "replace shrinks");
        check(new StringBuilder("abcdef").replace(1, 3, "").toString().equals("adef"), "replace with empty deletes the range");
        check(new StringBuilder("abc").replace(0, 3, "z").toString().equals("z"), "replace full range");
        StringBuilder r = new StringBuilder("abc");
        check(r.replace(1, 2, "Q") == r && r.toString().equals("aQc"), "replace returns this");
        expectClass(() -> new StringBuilder("abc").replace(0, 1, null), "java.lang.NullPointerException",
              "replace(0,1,null)");
        expectExact(() -> new StringBuilder("abc").replace(-1, 1, "x"),
              CLASS_SIOOBE,
              MODERN ? "Range [-1, 1) out of bounds for length 3" : "String index out of range: -1",
              "replace(-1,1,x)");
        expectExact(() -> new StringBuilder("abc").replace(2, 1, "x"),
              CLASS_SIOOBE,
              MODERN ? "Range [2, 1) out of bounds for length 3" : "start > end",
              "replace(2,1,x) start>end");
    }

    static void testSetCharAtSetLength() {
        StringBuilder h = new StringBuilder("abc");
        h.setCharAt(0, 'X');
        check(h.toString().equals("Xbc"), "setCharAt(0,..)");
        StringBuilder t = new StringBuilder("abc");
        t.setCharAt(2, 'X');
        check(t.toString().equals("abX"), "setCharAt(len-1,..)");
        StringBuilder n0 = new StringBuilder("abc");
        n0.setLength(0);
        check(n0.length() == 0, "setLength(0) empties");
        StringBuilder same = new StringBuilder("abc");
        same.setLength(3);
        check(same.toString().equals("abc"), "setLength(len) unchanged");
        StringBuilder tr = new StringBuilder("abcdef");
        tr.setLength(3);
        check(tr.length() == 3 && tr.toString().equals("abc"), "setLength shrink drops the tail");
        StringBuilder grow = new StringBuilder("ab");
        grow.setLength(4);
        check(grow.length() == 4 && grow.charAt(2) == '\u0000' && grow.charAt(3) == '\u0000',
              "setLength grow pads with NUL");
        grow.append("c");
        check(grow.length() == 5 && grow.charAt(4) == 'c' && grow.charAt(2) == '\u0000',
              "append lands right after the NUL padding");
        // The negative setLength message is byte-identical on JDK 8 and JDK 27: lock it.
        expectExact(() -> new StringBuilder("abc").setLength(-1),
              CLASS_SIOOBE, "String index out of range: -1", "setLength(-1)");
        expectExact(() -> new StringBuilder("abc").setLength(Integer.MIN_VALUE),
              CLASS_SIOOBE, "String index out of range: -2147483648", "setLength(MIN_VALUE)");
        expectExact(() -> new StringBuilder("abc").setCharAt(-1, 'x'),
              CLASS_SIOOBE,
              MODERN ? "Index -1 out of bounds for length 3" : "String index out of range: -1",
              "setCharAt(-1,..)");
        expectExact(() -> new StringBuilder("abc").setCharAt(3, 'x'),
              CLASS_SIOOBE,
              MODERN ? "Index 3 out of bounds for length 3" : "String index out of range: 3",
              "setCharAt(len,..)");
    }

    static void testCapacityContract() {
        check(new StringBuilder().capacity() == 16, "default capacity is 16");
        check(new StringBuilder("abc").capacity() == 19, "String ctor capacity = str.length() + 16");
        check(new StringBuilder((CharSequence) "hello").capacity() == 21, "CharSequence ctor capacity = csq.length() + 16");
        check(new StringBuilder(0).capacity() == 0, "zero-capacity ctor is legal");
        StringBuilder noShrink = new StringBuilder("abc");
        noShrink.ensureCapacity(-5);
        check(noShrink.capacity() == 19, "ensureCapacity(negative) is a no-op");
        noShrink.ensureCapacity(0);
        check(noShrink.capacity() == 19, "ensureCapacity(0) never shrinks");
        noShrink.ensureCapacity(10);
        check(noShrink.capacity() == 19, "ensureCapacity(below current) keeps capacity");
        noShrink.ensureCapacity(100);
        check(noShrink.capacity() >= 100, "ensureCapacity(minimum) grows to at least minimum");
        StringBuilder grow = new StringBuilder();
        for (int i = 0; i < 40; i++) grow.append("0123456789");
        check(grow.length() == 400 && grow.capacity() >= grow.length(), "capacity >= length invariant after growth");
        StringBuilder trim = new StringBuilder("hello");
        trim.trimToSize();
        check(trim.capacity() == 5 && trim.length() == 5, "trimToSize makes capacity == length");
        trim.trimToSize();
        check(trim.capacity() == 5, "trimToSize is idempotent");
        trim.append("!");
        check(trim.toString().equals("hello!"), "append still works after trimToSize");
        StringBuilder empt = new StringBuilder("temp");
        empt.setLength(0);
        empt.trimToSize();
        check(empt.capacity() == 0, "trimToSize down to length 0");
    }

    static void testCodePointFamily() {
        // "a<UD834 UDD1E>b": one BMP char, one supplementary pair, one BMP char.
        StringBuilder sb = new StringBuilder("a\ud834\udd1eb");
        check(sb.length() == 4, "length counted in UTF-16 units");
        check(sb.charAt(1) == '\ud834' && sb.charAt(2) == '\udd1e', "charAt exposes both surrogate units");
        check(sb.codePointAt(0) == 'a', "codePointAt(0)");
        check(sb.codePointAt(1) == 0x1D11E, "codePointAt(high surrogate index) reads the whole pair");
        check(sb.codePointAt(2) == 0xDD1E, "codePointAt(lone low surrogate index) returns the unit itself");
        check(sb.codePointBefore(1) == 'a', "codePointBefore(1)");
        check(sb.codePointBefore(2) == 0xD834, "codePointBefore(2) sees unpaired high unit");
        check(sb.codePointBefore(3) == 0x1D11E, "codePointBefore(3) reads the pair backwards");
        check(sb.codePointBefore(4) == 'b', "codePointBefore(len)");
        check(sb.codePointCount(0, 4) == 3, "codePointCount over the full range");
        check(sb.codePointCount(1, 3) == 1, "codePointCount over the pair");
        check(sb.codePointCount(1, 2) == 1, "codePointCount over half a pair");
        check(sb.codePointCount(0, 0) == 0, "codePointCount over an empty range");
        check(sb.offsetByCodePoints(0, 1) == 1, "offsetByCodePoints 0->1");
        check(sb.offsetByCodePoints(0, 2) == 3, "offsetByCodePoints skips the whole pair");
        check(sb.offsetByCodePoints(1, 1) == 3, "offsetByCodePoints from inside the pair");
        check(sb.offsetByCodePoints(3, 1) == 4, "offsetByCodePoints over the last char");
        check(sb.offsetByCodePoints(4, 0) == 4, "offsetByCodePoints with zero shift");
        StringBuilder abc = new StringBuilder("abc");
        expectExact(() -> abc.codePointAt(-1),
              CLASS_SIOOBE,
              MODERN ? "Index -1 out of bounds for length 3" : "String index out of range: -1",
              "codePointAt(-1)");
        expectExact(() -> abc.codePointAt(3),
              CLASS_SIOOBE,
              MODERN ? "Index 3 out of bounds for length 3" : "String index out of range: 3",
              "codePointAt(len)");
        expectExact(() -> abc.codePointBefore(0),
              CLASS_SIOOBE,
              MODERN ? "Index -1 out of bounds for length 3" : "String index out of range: 0",
              "codePointBefore(0)");
        expectExact(() -> abc.codePointBefore(4),
              CLASS_SIOOBE,
              MODERN ? "Index 3 out of bounds for length 3" : "String index out of range: 4",
              "codePointBefore(len+1)");
        expectExact(() -> abc.codePointCount(2, 1),
              CLASS_IOOBE,
              MODERN ? "Range [2, 1) out of bounds for length 3" : null,
              "codePointCount(begin>end)");
        // Plain IndexOutOfBoundsException with a null message on BOTH releases: lock it.
        expectExact(() -> abc.offsetByCodePoints(0, 4),
              CLASS_IOOBE, null, "offsetByCodePoints past the end");
        expectExact(() -> abc.offsetByCodePoints(-1, 1),
              CLASS_IOOBE, null, "offsetByCodePoints(negative index)");
    }

    static void testReverseMatrix() {
        check(new StringBuilder().reverse().length() == 0, "reverse of empty stays empty");
        check(new StringBuilder("x").reverse().toString().equals("x"), "reverse of a single char");
        StringBuilder r = new StringBuilder("abc");
        check(r.reverse() == r && r.toString().equals("cba"), "reverse returns this");
        StringBuilder pali = new StringBuilder("aba");
        check(pali.reverse().toString().equals("aba"), "reverse of a palindrome equals the original");
        // reverse treats a surrogate pair as a single unit and keeps it well-formed.
        StringBuilder sur = new StringBuilder("a\ud834\udd1eb");
        sur.reverse();
        check(sur.length() == 4, "reverse preserves the UTF-16 length");
        check(sur.charAt(0) == 'b' && sur.charAt(1) == '\ud834' && sur.charAt(2) == '\udd1e' && sur.charAt(3) == 'a',
              "reverse moves the pair as one unit (\"b<clef>a\")");
        check(sur.codePointCount(0, 4) == 3, "reversed content still holds 3 code points");
        check(sur.codePointAt(1) == 0x1D11E, "the reversed pair still decodes");
        StringBuilder pair = new StringBuilder("\ud834\udd1e");
        pair.reverse();
        check(pair.charAt(0) == '\ud834' && pair.charAt(1) == '\udd1e', "a lone pair reverses to itself");
        StringBuilder loneHigh = new StringBuilder("\ud800");
        loneHigh.reverse();
        check(loneHigh.charAt(0) == '\ud800', "an unpaired high surrogate reverses to itself");
        StringBuilder loneLow = new StringBuilder("\udc00ab");
        loneLow.reverse();
        check(loneLow.charAt(0) == 'b' && loneLow.charAt(1) == 'a' && loneLow.charAt(2) == '\udc00',
              "an unpaired low surrogate travels as a plain unit");
        StringBuilder twice = new StringBuilder("stressed");
        twice.reverse().reverse();
        check(twice.toString().equals("stressed"), "reverse twice is the identity");
    }

    static void testSubstringSubSequence() {
        StringBuilder sb = new StringBuilder("abcdef");
        check(sb.substring(0).equals("abcdef"), "substring(0) whole content");
        check(sb.substring(6).equals(""), "substring(len) is empty");
        check(sb.substring(2, 6).equals("cdef"), "substring(2,len)");
        check(sb.substring(0, 0).equals(""), "substring(0,0) is empty");
        check(sb.substring(2, 2).equals(""), "substring(mid,mid) is empty");
        String a1 = sb.substring(1);
        String a2 = sb.substring(1);
        check(a1.equals(a2) && a1 != a2, "substring allocates a fresh String per call");
        CharSequence sub = sb.subSequence(1, 4);
        check(sub.getClass() == String.class, "subSequence returns a String");
        check(sub.toString().equals("bcd") && sub.length() == 3 && sub.charAt(0) == 'b', "subSequence content");
        check(sb.subSequence(6, 6).toString().equals(""), "subSequence(len,len) is empty");
        StringBuilder abc = new StringBuilder("abc");
        expectExact(() -> abc.substring(-1),
              CLASS_SIOOBE,
              MODERN ? "Range [-1, 3) out of bounds for length 3" : "String index out of range: -1",
              "substring(-1)");
        expectExact(() -> abc.substring(4),
              CLASS_SIOOBE,
              MODERN ? "Range [4, 3) out of bounds for length 3" : "String index out of range: -1",
              "substring(len+1)");
        expectExact(() -> abc.substring(2, 1),
              CLASS_SIOOBE,
              MODERN ? "Range [2, 1) out of bounds for length 3" : "String index out of range: -1",
              "substring(begin>end)");
        expectExact(() -> abc.substring(0, 4),
              CLASS_SIOOBE,
              MODERN ? "Range [0, 4) out of bounds for length 3" : "String index out of range: 4",
              "substring(end past length)");
        expectExact(() -> abc.subSequence(2, 1),
              CLASS_SIOOBE,
              MODERN ? "Range [2, 1) out of bounds for length 3" : "String index out of range: -1",
              "subSequence(begin>end)");
        expectExact(() -> abc.subSequence(0, 4),
              CLASS_SIOOBE,
              MODERN ? "Range [0, 4) out of bounds for length 3" : "String index out of range: 4",
              "subSequence(end past length)");
    }

    static void testIndexOfLastIndexOf() {
        StringBuilder sb = new StringBuilder("abc");
        check(sb.indexOf("") == 0, "indexOf(\"\") is 0");
        check(sb.indexOf("", 1) == 1, "indexOf(\"\",1)");
        check(sb.indexOf("", 3) == 3, "indexOf(\"\",len)");
        check(sb.indexOf("", 4) == 3, "indexOf(\"\",past len) clamps to len");
        check(sb.lastIndexOf("") == 3, "lastIndexOf(\"\") is len");
        check(sb.lastIndexOf("", 2) == 2, "lastIndexOf(\"\",2)");
        check(sb.lastIndexOf("", -1) == -1, "lastIndexOf(\"\",-1) is -1");
        check(sb.indexOf("abc") == 0, "indexOf whole content");
        check(sb.indexOf("abcd") == -1, "needle longer than haystack");
        check(sb.indexOf("b", 9) == -1, "fromIndex past length");
        StringBuilder aaa = new StringBuilder("aaa");
        check(aaa.indexOf("aa") == 0, "indexOf overlapping prefix");
        check(aaa.indexOf("aa", 1) == 1, "indexOf from inside the overlap");
        check(aaa.lastIndexOf("aa") == 1, "lastIndexOf prefers the last match");
        check(new StringBuilder("abc").indexOf("B") == -1, "indexOf is case sensitive");
        StringBuilder uni = new StringBuilder("a\ud834\udd1eb");
        check(uni.indexOf("\ud834\udd1e") == 1, "indexOf finds a supplementary needle");
        check(uni.lastIndexOf("\ud834\udd1e") == 1, "lastIndexOf with a supplementary needle");
        StringBuilder empty = new StringBuilder();
        check(empty.indexOf("") == 0 && empty.lastIndexOf("") == 0, "empty builder, empty needle");
        check(empty.indexOf("a") == -1 && empty.lastIndexOf("a") == -1, "empty builder, missing needle");
        StringBuilder ban = new StringBuilder("banana");
        check(ban.lastIndexOf("a", 5) == 5 && ban.lastIndexOf("a", 4) == 3, "lastIndexOf(str,from) scans backwards");
    }

    static void testToStringSnapshot() {
        StringBuilder sb = new StringBuilder("hello");
        String snap = sb.toString();
        sb.append(" world");
        check(snap.equals("hello"), "toString snapshot is independent of later appends");
        sb.setCharAt(0, 'H');
        check(snap.equals("hello"), "snapshot unaffected by setCharAt");
        String s1 = sb.toString();
        String s2 = sb.toString();
        check(s1.equals(s2), "two toString calls are equal");
        check(s1 != s2, "each toString call allocates a new String");
        check(s1.equals("Hello world"), "toString reflects the current content");
        sb.delete(0, sb.length());
        check(sb.toString().equals(""), "toString of an emptied builder");
        StringBuilder hw = new StringBuilder("hello");
        check("hello".contentEquals(hw), "String.contentEquals sees the same chars");
        check(!"hell".contentEquals(hw), "contentEquals detects different content");
    }

    static void testEqualsHashCode() {
        // StringBuilder does not override equals/hashCode: identity semantics.
        StringBuilder a = new StringBuilder("a");
        StringBuilder b = new StringBuilder("a");
        check(!a.equals(b), "two same-content builders are NOT equals");
        check(a.equals(a), "equals is reflexive");
        check(!a.equals(null), "equals(null) is false");
        check(!a.equals("a"), "equals against a String is false");
        check(a.hashCode() == a.hashCode(), "hashCode is stable across calls");
        check(a.toString().equals(b.toString()), "toString().equals bridges equal content");
        check(!a.equals(b), "contrast locked: content equal, instances not");
    }

    static void testCharSequencePolymorphism() {
        CharSequence cs = new StringBuilder("hello");
        check(cs.length() == 5, "CharSequence.length");
        check(cs.charAt(1) == 'e', "CharSequence.charAt");
        check(cs.subSequence(1, 3).toString().equals("el"), "CharSequence.subSequence");
        check(cs.toString().equals("hello"), "CharSequence.toString");
        check(cs.chars().count() == 5, "default chars() streams over the builder");
        check(cs.codePoints().count() == 5, "default codePoints() streams over the builder");
        CharSequence empty = new StringBuilder();
        check(empty.length() == 0, "empty builder viewed as CharSequence");
        StringBuilder sink = new StringBuilder("[");
        sink.append(cs, 1, 4);
        sink.append(']');
        check(sink.toString().equals("[ell]"), "append(CharSequence,int,int) composed from a builder view");
    }

    static void testGetCharsMatrix() {
        char[] full = new char[3];
        new StringBuilder("abc").getChars(0, 3, full, 0);
        check(full[0] == 'a' && full[1] == 'b' && full[2] == 'c', "getChars full copy");
        char[] part = new char[4];
        new StringBuilder("abcd").getChars(1, 4, part, 1);
        check(part[0] == '\0' && part[1] == 'b' && part[2] == 'c' && part[3] == 'd',
              "getChars honors dst offset and leaves other slots untouched");
        char[] big = new char[8];
        new StringBuilder("abc").getChars(0, 3, big, 5);
        check(big[5] == 'a' && big[6] == 'b' && big[7] == 'c', "getChars into the tail of a large destination");
        StringBuilder sb = new StringBuilder("abc");
        expectExact(() -> sb.getChars(2, 1, new char[3], 0),
              CLASS_SIOOBE,
              MODERN ? "Range [2, 1) out of bounds for length 3" : "srcBegin > srcEnd",
              "getChars(srcBegin>srcEnd)");
        expectExact(() -> sb.getChars(0, 3, new char[2], 0),
              MODERN ? CLASS_IOOBE : CLASS_AIOOBE,
              MODERN ? "Range [0, 3) out of bounds for length 2" : null,
              "getChars destination too small");
        expectExact(() -> sb.getChars(0, 3, new char[3], 5),
              MODERN ? CLASS_IOOBE : CLASS_AIOOBE,
              MODERN ? "Range [5, 8) out of bounds for length 3" : null,
              "getChars destination offset out of range");
    }

    static void testNegativeCapacityCtorMessage() {
        expectExact(() -> new StringBuilder(-1),
              "java.lang.NegativeArraySizeException",
              MODERN ? "-1" : null,
              "new StringBuilder(-1) message");
    }

    static void testCharAtExactMessages() {
        StringBuilder abc = new StringBuilder("abc");
        expectExact(() -> abc.charAt(-1),
              CLASS_SIOOBE,
              MODERN ? "Index -1 out of bounds for length 3" : "String index out of range: -1",
              "charAt(-1)");
        expectExact(() -> abc.charAt(3),
              CLASS_SIOOBE,
              MODERN ? "Index 3 out of bounds for length 3" : "String index out of range: 3",
              "charAt(len)");
        check(abc.toString().equals("abc"), "failed charAt leaves content intact");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testConstructorExceptions();
        testAppend();
        testAppendNullSemantics();
        testAppendCodePoint();
        testLengthChars();
        testExceptionMatrix();
        testInsertDelete();
        testInsertOverloads();
        testReverseSubstringIndex();
        testIndexOfFromIndex();
        testCapacity();
        testStreams();
        testNotSynchronized();
        testBoundaryEquivalence();
        testWhiteBox();
        testAppendPrimitivesExact();
        testAppendCharSequenceSources();
        testAppendSelfAndCross();
        testAppendCodePointEdges();
        testAppendCharArrayBoundsExact();
        testInsertBoundariesAndTypes();
        testDeleteMatrix();
        testReplaceMatrix();
        testSetCharAtSetLength();
        testCapacityContract();
        testCodePointFamily();
        testReverseMatrix();
        testSubstringSubSequence();
        testIndexOfLastIndexOf();
        testToStringSnapshot();
        testEqualsHashCode();
        testCharSequencePolymorphism();
        testGetCharsMatrix();
        testNegativeCapacityCtorMessage();
        testCharAtExactMessages();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
