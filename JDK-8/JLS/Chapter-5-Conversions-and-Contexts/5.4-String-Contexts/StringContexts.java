/*
 * @test
 * @summary JLS 5.4/5.1.11 (Java SE 8): a string context applies to the non-String
 *          operand of binary + when the other operand is a String. The operand
 *          undergoes string conversion; a null reference becomes "null".
 * @run main StringContexts
 */

public class StringContexts {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // String conversion of primitives.
        check("x" .equals("" + "x"), "String identity");
        check("1".equals("" + 1), "int -> decimal string");
        check("true".equals("" + true), "boolean -> \"true\"");
        check("65".equals("" + 65), "int -> \"65\"");
        check("A".equals("" + 'A'), "char -> its character");
        check("1.5".equals("" + 1.5d), "double -> decimal string");

        // null reference -> "null".
        check("null".equals("" + (Object) null), "null reference -> \"null\"");
        check("null".equals("" + (String) null), "null String -> \"null\"");

        // The toString result is used; null toString result becomes "null".
        check("obj".equals("" + new Object() {
            public String toString() { return "obj"; }
        }), "toString result is used");
        check("null".equals("" + new Object() {
            public String toString() { return null; }
        }), "null toString result becomes \"null\"");

        // Left-to-right associativity of +.
        check("x12".equals("x" + 1 + 2), "left-to-right: string then numeric");
        check("3x".equals(1 + 2 + "x"), "numeric addition before concatenation");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
