/*
 * @test
 * @summary JLS 15.18.1: the string concatenation operator + is left-associative;
 *          non-String operands are converted to String at the point they are
 *          encountered (null becomes "null"), and a char is concatenated as a
 *          single character rather than promoted to int.
 * @run main StringConcatenation
 */

public class StringConcatenation {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check((1 + 2 + "s").equals("3s"), "(1+2)+\"s\" == \"3s\"");
        check(("s" + 1 + 2).equals("s12"), "\"s\"+1+2 == \"s12\"");
        check(("" + 1 + 2).equals("12"), "\"\"+1+2 == \"12\"");
        check(("x" + null).equals("xnull"), "null -> \"null\"");
        check((null + "x").equals("nullx"), "null on the left");
        check(("a" + 'b').equals("ab"), "char concatenated as a character");
        check(("v=" + true).equals("v=true"), "boolean concatenation");
        check(("f=" + 1.5).equals("f=1.5"), "double concatenation");
        check((1 + "x" + 2 + 3).equals("1x23"), "mixed left-to-right");
        check(('a' + 1) == 98, "'a'+1 is int arithmetic, not string");
        check(("" + 'a' + 1).equals("a1"), "\"\"+'a'+1 == \"a1\"");
        check((1 + 2 + "x" + 3 + 4).equals("3x34"), "numeric until String, then concat");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
