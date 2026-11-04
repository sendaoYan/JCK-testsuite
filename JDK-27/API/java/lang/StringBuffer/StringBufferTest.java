/*
 * @test
 * @summary java.lang.StringBuffer API conformance: constructors and capacity,
 *          append overloads + appendCodePoint (chaining returns this), length/
 *          charAt/setCharAt/setLength/getChars, insert/delete/deleteCharAt/replace,
 *          reverse/substring/subSequence/indexOf, code points, ensureCapacity/trimToSize.
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

    static void testConstructors() {
        StringBuffer a = new StringBuffer();
        check(a.capacity() == 16 && a.length() == 0, "default ctor capacity 16");
        check(new StringBuffer("abc").capacity() == 19, "String ctor capacity 16+3");
        check(new StringBuffer("abc").toString().equals("abc"), "String ctor content");
        check(new StringBuffer(50).capacity() == 50, "int ctor capacity");
        CharSequence cs = "hello";
        check(new StringBuffer(cs).toString().equals("hello"), "CharSequence ctor");
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

    static void testAppendCodePoint() {
        StringBuffer sb = new StringBuffer();
        sb.appendCodePoint(0x1D518);
        check(sb.length() == 2, "appendCodePoint length +2");
        check(sb.codePointAt(0) == 0x1D518, "appendCodePoint value");
        check(sb.codePointCount(0, 2) == 1, "codePointCount");
        check(sb.codePointBefore(2) == 0x1D518, "codePointBefore");
        check(sb.offsetByCodePoints(0, 1) == 2, "offsetByCodePoints");
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

    static void testCapacity() {
        StringBuffer sb = new StringBuffer(8);
        sb.ensureCapacity(32);
        check(sb.capacity() >= 32, "ensureCapacity grows");
        sb.append("hello");
        sb.trimToSize();
        check(sb.capacity() == 5, "trimToSize capacity == length");
    }

    static boolean isSynchronized(Class<?> c, String name, Class<?>... params) throws Exception {
        return java.lang.reflect.Modifier.isSynchronized(
            c.getMethod(name, params).getModifiers());
    }

    static void testCompareTo() {
        check(new StringBuffer("abc").compareTo(new StringBuffer("abc")) == 0, "compareTo equal");
        check(new StringBuffer("abc").compareTo(new StringBuffer("abd")) < 0, "compareTo <");
        check(new StringBuffer("abd").compareTo(new StringBuffer("abc")) > 0, "compareTo >");
        check(new StringBuffer("ab").compareTo(new StringBuffer("abc")) < 0, "compareTo prefix");
        Comparable<StringBuffer> c = new StringBuffer("x");
        check(c.compareTo(new StringBuffer("x")) == 0, "compareTo via Comparable");
    }

    static void testIndexOfFromIndex() {
        StringBuffer sb = new StringBuffer("banana");
        check(sb.indexOf("an", 1) == 1, "indexOf(str,1)");
        check(sb.indexOf("an", 2) == 3, "indexOf(str,2)");
        check(sb.indexOf("zz", 0) == -1, "indexOf missing");
        check(sb.lastIndexOf("an", 5) == 3, "lastIndexOf(str,5)");
        check(sb.lastIndexOf("an", 2) == 1, "lastIndexOf(str,2)");
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
    }

    static void testStreams() {
        StringBuffer sb = new StringBuffer("ab");
        check(sb.chars().count() == 2, "chars count");
        check(sb.codePoints().count() == 2, "codePoints count");
        check(sb.chars().sum() == 'a' + 'b', "chars sum");
    }

    static void testSynchronized() throws Exception {
        check(isSynchronized(StringBuffer.class, "append", String.class),
              "StringBuffer.append synchronized");
        check(isSynchronized(StringBuffer.class, "insert", int.class, String.class),
              "StringBuffer.insert synchronized");
        check(isSynchronized(StringBuffer.class, "reverse"),
              "StringBuffer.reverse synchronized");
        check(isSynchronized(StringBuffer.class, "length"),
              "StringBuffer.length synchronized");
        check(isSynchronized(StringBuffer.class, "toString"),
              "StringBuffer.toString synchronized");
    }

    static String exercise(StringBuilder sb) {
        sb.append('a').append("bc").append(123);
        sb.insert(1, "!!").delete(2, 4).reverse().reverse();
        sb.setLength(4);
        sb.setCharAt(0, 'Z');
        return sb.toString();
    }

    static String exercise(StringBuffer sb) {
        sb.append('a').append("bc").append(123);
        sb.insert(1, "!!").delete(2, 4).reverse().reverse();
        sb.setLength(4);
        sb.setCharAt(0, 'Z');
        return sb.toString();
    }

    static void testBehaviorEquivalence() {
        String a = exercise(new StringBuilder());
        String b = exercise(new StringBuffer());
        check(a.equals("Z!c1"), "exercise expected Z!c1");
        check(a.equals(b), "StringBuilder and StringBuffer behavior identical");
    }

    static void testBoundaryEquivalence() {
        // reverse is an involution.
        StringBuffer sb = new StringBuffer("stressed");
        sb.reverse();
        check(sb.toString().equals("desserts"), "reverse('stressed')");
        sb.reverse();
        check(sb.toString().equals("stressed"), "reverse involution");

        // setLength(0) empties without changing capacity.
        StringBuffer c = new StringBuffer("abcdef");
        int cap = c.capacity();
        c.setLength(0);
        check(c.length() == 0 && c.capacity() == cap, "setLength(0) preserves capacity");

        // append((CharSequence) null) appends "null".
        StringBuffer n = new StringBuffer();
        n.append((CharSequence) null);
        check(n.toString().equals("null"), "append((CharSequence) null)");

        // insert at boundaries 0 and length.
        StringBuffer ins = new StringBuffer("ab");
        ins.insert(0, 'Z');
        check(ins.toString().equals("Zab"), "insert char at 0");
        ins.insert(ins.length(), '!');
        check(ins.toString().equals("Zab!"), "insert char at length");

        // delete(0,0) is a no-op.
        StringBuffer d = new StringBuffer("abc");
        d.delete(0, 0);
        check(d.toString().equals("abc"), "delete(0,0) no-op");

        // appendCodePoint BMP (single UTF-16 unit).
        StringBuffer bmp = new StringBuffer();
        bmp.appendCodePoint('A');
        check(bmp.length() == 1 && bmp.charAt(0) == 'A', "appendCodePoint BMP");

        // capacity auto-grows beyond the initial 16.
        StringBuffer g = new StringBuffer();
        for (int i = 0; i < 100; i++) g.append('x');
        check(g.length() == 100 && g.capacity() >= 100, "auto-grow capacity >= length");

        // toString -> new StringBuffer(String) round-trip.
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
        check(hasInterface(c, "java.lang.Appendable"), "implements Appendable");
        check(hasInterface(c, "java.io.Serializable"), "implements Serializable");
        check(hasInterface(c, "java.lang.Comparable"), "implements Comparable");
        check(hasInterface(c, "java.lang.CharSequence"), "implements CharSequence");
        check(hasMethod(c, "append", String.class), "method append(String)");
        check(hasMethod(c, "reverse"), "method reverse()");
        check(hasMethod(c, "compareTo", StringBuffer.class), "method compareTo(StringBuffer)");
    }

    // ---------- black-box: behavioral composition ----------
    static void testBlackBox() throws Exception {
        testConstructors();
        testAppend();
        testAppendCodePoint();
        testLengthChars();
        testInsertDelete();
        testReverseSubstringIndex();
        testCapacity();
        testCompareTo();
        testIndexOfFromIndex();
        testInsertOverloads();
        testStreams();
        testSynchronized();
        testBehaviorEquivalence();
        testBoundaryEquivalence();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
