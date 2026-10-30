/*
 * @test
 * @summary JLS 10.9: an array of char is not a String; a char array is mutable
 *          while a String is immutable, and neither is NUL-terminated.
 * @run main CharArrayNotString
 */

public class CharArrayNotString {
    public static void main(String[] args) {
        char[] ca = { 'a', 'b', 'c' };
        String s = new String(ca);

        Class<?> caClass = ca.getClass();
        Class<?> sClass = s.getClass();
        if (caClass == sClass)
            throw new AssertionError("char[] and String are different types");

        // char[] is mutable; String is immutable.
        ca[0] = 'z';
        if (ca[0] != 'z') throw new AssertionError("char[] mutable");
        if (!"abc".equals(s)) throw new AssertionError("String immutable");

        // The NUL character is a regular element, not a terminator.
        char[] withNul = { 'a', '\u0000', 'b' };
        if (withNul.length != 3) throw new AssertionError("NUL is not a terminator");
        System.out.println("PASS");
    }
}
