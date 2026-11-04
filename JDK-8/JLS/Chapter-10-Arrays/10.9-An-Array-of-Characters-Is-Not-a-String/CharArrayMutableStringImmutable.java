/*
 * @test
 * @summary JLS 10.9 (Java SE 8): a char[] has mutable elements while a String is
 *          immutable; toCharArray returns a new array with the same sequence.
 * @run main CharArrayMutableStringImmutable
 */

public class CharArrayMutableStringImmutable {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        char[] ca = { 'a', 'b' };
        ca[0] = 'z';
        check(ca[0] == 'z', "char[] elements are mutable");

        String s = "ab";
        String replaced = s.replace('a', 'z');
        check("ab".equals(s), "String is immutable");
        check("zb".equals(replaced), "replace returns new value");

        char[] copy = "hello".toCharArray();
        copy[0] = 'H';
        check("hello".charAt(0) == 'h', "mutating toCharArray copy does not affect String");

        char[] withNul = { 'a', '\u0000', 'b' };
        check(withNul.length == 3, "char[] length includes NUL element");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
