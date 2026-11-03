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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
