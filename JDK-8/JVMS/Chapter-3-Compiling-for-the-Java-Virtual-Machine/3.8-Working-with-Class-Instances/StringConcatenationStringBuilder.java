/*
 * @test
 * @summary JVMS 3.8/15.18.1: the compiler desugars the string concatenation
 *          operator + into StringBuilder.append calls, so each operand is
 *          converted to a String (null becomes "null", primitives are widened)
 *          and the pieces are appended in order.
 * @run main StringConcatenationStringBuilder
 */

public class StringConcatenationStringBuilder {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static String withObject(Object o) { return "value=" + o; }

    public static void main(String[] args) {
        check("x" + 1 == "x1", "int operand appends its decimal form");
        check("x" + true == "xtrue", "boolean operand appends true/false");
        check("x" + 'c' == "xc", "char operand appends the character");
        check("x" + 1L == "x1", "long operand appends its decimal form");
        check("x" + 1.5 == "x1.5", "double operand appends its decimal form");
        check("1" + "2" == "12", "string operands concatenate");
        check("value=null".equals(withObject(null)), "null operand becomes \"null\"");
        check("value=abc".equals(withObject("abc")), "object operand uses toString()");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
