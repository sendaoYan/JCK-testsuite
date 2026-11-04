/*
 * @test
 * @summary JLS 10.9: a char[] has mutable elements while a String is immutable;
 *          toCharArray returns a new array containing the same character sequence.
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
        check("ab".equals(s), "String is immutable: replace returns a new String");
        check("zb".equals(replaced), "replace produces the new value");

        // toCharArray returns a fresh array: mutating it does not affect the String.
        String src = "hello";
        char[] copy = src.toCharArray();
        check(copy.length == 5 && copy[0] == 'h', "toCharArray copies the sequence");
        copy[0] = 'H';
        check(src.charAt(0) == 'h', "mutating the copy does not affect the String");

        // No NUL terminator: a char[] may contain '\u0000' as an ordinary element.
        char[] withNul = { 'a', '\u0000', 'b' };
        check(withNul.length == 3, "char[] length includes NUL as an element");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
