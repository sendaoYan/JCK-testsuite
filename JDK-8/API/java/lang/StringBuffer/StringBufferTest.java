/*
 * @test
 * @summary java.lang.StringBuffer API conformance: constructors + capacity +
 *          negative-capacity/null boundary exceptions, append overloads +
 *          appendCodePoint (chaining returns this + supplementary), null-argument
 *          semantics matrix, length/charAt/setCharAt/setLength/getChars,
 *          insert/delete/deleteCharAt/replace, reverse/substring/subSequence/indexOf
 *          (incl. fromIndex), code points, ensureCapacity/trimToSize, streams, and
 *          the structural contract (final, extends AbstractStringBuilder, implements
 *          Appendable/CharSequence/Serializable, and thread-safe synchronized methods).
 * @run main StringBufferTest
 */

public class StringBufferTest {
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
        StringBuffer a = new StringBuffer();
        check(a.capacity() == 16 && a.length() == 0, "default ctor capacity 16");
        check(new StringBuffer("abc").capacity() == 19, "String ctor capacity 16+3");
        check(new StringBuffer("abc").toString().equals("abc"), "String ctor content");
        check(new StringBuffer(50).capacity() == 50, "int ctor capacity");
        CharSequence cs = "hello";
        check(new StringBuffer(cs).toString().equals("hello"), "CharSequence ctor");
        StringBuffer z = new StringBuffer(0);
        check(z.capacity() == 0 && z.length() == 0, "capacity 0 legal");
        z.append('x');
        check(z.toString().equals("x") && z.capacity() >= 1, "capacity 0 grows on append");
    }

    static void testConstructorExceptions() {
        try {
            new StringBuffer(-1);
            throw new AssertionError("new StringBuffer(-1): expected NegativeArraySizeException");
        } catch (NegativeArraySizeException expected) {
            passed++;
        }
        expectNPE(() -> new StringBuffer((String) null), "StringBuffer((String)null)");
        expectNPE(() -> new StringBuffer((CharSequence) null), "StringBuffer((CharSequence)null)");
    }

    static void testAppend() {
        StringBuffer sb = new StringBuffer();
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
        check(new StringBuffer().append((String) null).toString().equals("null"),
              "append((String)null) == \"null\"");
        check(new StringBuffer().append((Object) null).toString().equals("null"),
              "append((Object)null) == \"null\"");
        check(new StringBuffer().append((CharSequence) null).toString().equals("null"),
              "append((CharSequence)null) == \"null\"");
        expectNPE(() -> new StringBuffer().append((char[]) null), "append((char[])null)");
        expectNPE(() -> new StringBuffer().append((char[]) null, 0, 0),
                  "append((char[])null,off,len)");
        check(new StringBuffer().append((CharSequence) null, 0, 4).toString().equals("null"),
              "append((CharSequence)null,0,4) == \"null\"");
        expectIOOBE(() -> new StringBuffer().append((CharSequence) null, 0, 5),
                    "append((CharSequence)null,0,5) past \"null\" length");
        expectIOOBE(() -> new StringBuffer().append(new char[]{'a'}, -1, 1),
                    "append(char[],-1,1)");
        expectIOOBE(() -> new StringBuffer().append(new char[]{'a'}, 0, 2),
                    "append(char[],0,2)");
        expectIOOBE(() -> new StringBuffer().append("ab", -1, 1),
                    "append(CharSequence,-1,1)");
        expectIOOBE(() -> new StringBuffer().append("ab", 0, 3),
                    "append(CharSequence,0,3)");
    }

    static void testAppendCodePoint() {
        StringBuffer sb = new StringBuffer();
        sb.appendCodePoint(0x1D518);
        check(sb.length() == 2, "appendCodePoint length +2");
        check(sb.codePointAt(0) == 0x1D518, "appendCodePoint value");
        check(sb.codePointCount(0, 2) == 1, "codePointCount");
        check(sb.codePointBefore(2) == 0x1D518, "codePointBefore");
        check(sb.offsetByCodePoints(0, 1) == 2, "offsetByCodePoints");

        StringBuffer bmp = new StringBuffer();
        bmp.appendCodePoint('A');
        check(bmp.length() == 1 && bmp.charAt(0) == 'A', "appendCodePoint BMP");

        expectIAE(() -> new StringBuffer().appendCodePoint(Character.MIN_CODE_POINT - 1),
                  "appendCodePoint(negative)");
        expectIAE(() -> new StringBuffer().appendCodePoint(Character.MAX_CODE_POINT + 1),
                  "appendCodePoint(> MAX_CODE_POINT)");
    }

    static void testLengthChars() {
        StringBuffer sb = new StringBuffer("abc");
        check(sb.length() == 3 && sb.charAt(1) == 'b', "length/charAt");
        sb.setCharAt(1, 'X');
        check(sb.toString().equals("aXc"), "setCharAt");
        sb.setLength(2);
        check(sb.toString().equals("aX"), "setLength truncate");
        sb.setLength(5);
        check(sb.length() == 5 && sb.charAt(3) == '\0', "setLength pads with NUL");

        char[] dst = new char[3];
        new StringBuffer("abc").getChars(0, 3, dst, 0);
        check(dst[0] == 'a' && dst[2] == 'c', "getChars");
        expectSIOOBE(() -> sb.charAt(-1), "charAt(-1)");
        expectSIOOBE(() -> sb.charAt(5), "charAt(len)");
    }

    static void testExceptionMatrix() {
        StringBuffer sb = new StringBuffer("abc");
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

        StringBuffer d = new StringBuffer("abcdef");
        expectSIOOBE(() -> d.delete(-1, 1), "delete(-1,1)");
        expectSIOOBE(() -> d.delete(2, 1), "delete(begin>end)");
        expectSIOOBE(() -> d.deleteCharAt(-1), "deleteCharAt(-1)");
        expectSIOOBE(() -> d.deleteCharAt(6), "deleteCharAt(len)");
        expectSIOOBE(() -> d.replace(-1, 1, "x"), "replace(-1,1)");
        expectSIOOBE(() -> d.replace(7, 1, "x"), "replace(start>end after clamp)");
        check(new StringBuffer("abcdef").delete(0, 7).toString().equals(""),
              "delete(0,len+1) clamps end to empty");
        check(new StringBuffer("abcdef").replace(0, 7, "x").toString().equals("x"),
              "replace(0,len+1) clamps end");

        expectNPE(() -> sb.indexOf((String) null), "indexOf((String)null)");
        expectNPE(() -> sb.indexOf((String) null, 0), "indexOf((String)null,0)");
        expectNPE(() -> sb.lastIndexOf((String) null), "lastIndexOf((String)null)");
        expectNPE(() -> sb.lastIndexOf((String) null, 0), "lastIndexOf((String)null,0)");
        expectNPE(() -> sb.insert(0, (char[]) null), "insert((char[])null)");
        expectNPE(() -> sb.insert(0, (char[]) null, 0, 0), "insert((char[])null,off,len)");
    }

    static void testInsertDelete() {
        StringBuffer sb = new StringBuffer("abcd");
        sb.insert(2, "XY");
        check(sb.toString().equals("abXYcd"), "insert String");
        sb.insert(0, true);
        check(sb.toString().equals("trueabXYcd"), "insert boolean");
        sb.insert(sb.length(), '!');
        check(sb.toString().equals("trueabXYcd!"), "insert char at end");

        StringBuffer d = new StringBuffer("abcdef");
        d.delete(1, 3);
        check(d.toString().equals("adef"), "delete(1,3)");
        d.deleteCharAt(0);
        check(d.toString().equals("def"), "deleteCharAt(0)");
        d.replace(1, 3, "XY");
        check(d.toString().equals("dXY"), "replace(1,3,XY)");
        // JDK-8 throws ArrayIndexOutOfBoundsException here, JDK-27 throws
        // StringIndexOutOfBoundsException; both are IndexOutOfBoundsException.
        expectIOOBE(() -> new StringBuffer("ab").insert(5, 'x'), "insert past end");
    }

    static void testInsertOverloads() {
        StringBuffer sb = new StringBuffer("x");
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
        check(new StringBuffer().insert(0, (String) null).toString().equals("null"),
              "insert((String)null) == \"null\"");
        check(new StringBuffer().insert(0, (Object) null).toString().equals("null"),
              "insert((Object)null) == \"null\"");
        check(new StringBuffer().insert(0, (CharSequence) null).toString().equals("null"),
              "insert((CharSequence)null) == \"null\"");
    }

    static void testReverseSubstringIndex() {
        StringBuffer sb = new StringBuffer("abc");
        check(sb.reverse().toString().equals("cba"), "reverse");
        check(sb.substring(1).equals("ba"), "substring(1)");
        check(sb.substring(1, 2).equals("b"), "substring(1,2)");
        check(sb.subSequence(0, 2).equals("cb"), "subSequence");
        check(sb.indexOf("b") == 1, "indexOf");
        check(sb.lastIndexOf("b") == 1, "lastIndexOf");
        check(sb.indexOf("z") == -1, "indexOf missing");
    }

    static void testIndexOfFromIndex() {
        StringBuffer sb = new StringBuffer("banana");
        check(sb.indexOf("an", 1) == 1, "indexOf(str,1)");
        check(sb.indexOf("an", 2) == 3, "indexOf(str,2)");
        check(sb.indexOf("zz", 0) == -1, "indexOf missing");
        check(sb.lastIndexOf("an", 5) == 3, "lastIndexOf(str,5)");
        check(sb.lastIndexOf("an", 2) == 1, "lastIndexOf(str,2)");
    }

    static void testCapacity() {
        StringBuffer sb = new StringBuffer(8);
        sb.ensureCapacity(32);
        check(sb.capacity() >= 32, "ensureCapacity grows");
        sb.append("hello");
        sb.trimToSize();
        check(sb.capacity() == 5, "trimToSize capacity == length");
    }

    static void testStreams() {
        StringBuffer sb = new StringBuffer("ab");
        check(sb.chars().count() == 2, "chars count");
        check(sb.codePoints().count() == 2, "codePoints count");
        check(sb.chars().sum() == 'a' + 'b', "chars sum");
    }

    static boolean isSynchronized(Class<?> c, String name, Class<?>... params) throws Exception {
        return java.lang.reflect.Modifier.isSynchronized(
            c.getMethod(name, params).getModifiers());
    }

    static void testSynchronized() throws Exception {
        check(isSynchronized(StringBuffer.class, "append", String.class),
              "StringBuffer.append synchronized");
        check(isSynchronized(StringBuffer.class, "reverse"), "StringBuffer.reverse synchronized");
        check(isSynchronized(StringBuffer.class, "toString"), "StringBuffer.toString synchronized");
    }

    static void testBoundaryEquivalence() {
        StringBuffer sb = new StringBuffer("stressed");
        sb.reverse();
        check(sb.toString().equals("desserts"), "reverse('stressed')");
        sb.reverse();
        check(sb.toString().equals("stressed"), "reverse involution");

        StringBuffer c = new StringBuffer("abcdef");
        int cap = c.capacity();
        c.setLength(0);
        check(c.length() == 0 && c.capacity() == cap, "setLength(0) preserves capacity");

        StringBuffer ins = new StringBuffer("ab");
        ins.insert(0, 'Z');
        check(ins.toString().equals("Zab"), "insert char at 0");
        ins.insert(ins.length(), '!');
        check(ins.toString().equals("Zab!"), "insert char at length");

        StringBuffer d = new StringBuffer("abc");
        d.delete(0, 0);
        check(d.toString().equals("abc"), "delete(0,0) no-op");

        StringBuffer g = new StringBuffer();
        for (int i = 0; i < 100; i++) g.append('x');
        check(g.length() == 100 && g.capacity() >= 100, "auto-grow capacity >= length");

        check(new StringBuffer(g.toString()).toString().equals(g.toString()), "toString round-trip");
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
        Class<?> c = Class.forName("java.lang.StringBuffer");
        check(!c.isInterface(), "StringBuffer not an interface");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "StringBuffer is final");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "StringBuffer not abstract");
        check(c.getSuperclass().getName().equals("java.lang.AbstractStringBuilder"),
              "extends AbstractStringBuilder");
        check(java.lang.Appendable.class.isAssignableFrom(c), "implements Appendable");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(hasInterface(c, "java.lang.CharSequence"), "implements CharSequence");
        check(hasMethod(c, "append", String.class), "method append(String)");
        check(hasMethod(c, "reverse"), "method reverse()");
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
        testSynchronized();
        testBoundaryEquivalence();
        testWhiteBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
