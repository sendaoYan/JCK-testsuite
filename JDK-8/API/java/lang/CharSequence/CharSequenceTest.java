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

    public static void main(String[] args) {
        testStringAsCharSequence();
        testCharAtBounds();
        testSubSequenceBounds();
        testStreams();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
