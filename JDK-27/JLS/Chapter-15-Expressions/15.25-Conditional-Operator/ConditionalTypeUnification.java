/*
 * @test
 * @summary JLS 15.25: the type of a conditional expression is determined by the
 *          second and third operands: integral operands are subject to binary
 *          numeric promotion, a primitive and its boxed counterpart unbox to the
 *          primitive, and two reference operands yield their least upper bound.
 * @run main ConditionalTypeUnification
 */

public class ConditionalTypeUnification {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean cond(boolean v) { return v; }

    public static void main(String[] args) {
        check((cond(true) ? 1 : 2) == 1, "int/int -> int");
        check((cond(true) ? (byte) 1 : (short) 2) == 1, "byte/short -> int (numeric promotion)");
        check((cond(true) ? 'a' : 2) == 97, "char/int -> int");
        check((cond(true) ? 1 : 2L) == 1L, "int/long -> long");
        check((cond(true) ? 1 : 2.0) == 1.0, "int/double -> double");
        check((cond(true) ? 1.0f : 2.0) == 1.0, "float/double -> double");

        Object r = cond(true) ? "a" : "b";
        check(r.equals("a"), "String/String -> String");

        Number n = cond(true) ? Integer.valueOf(1) : Double.valueOf(2.0);
        check(n.intValue() == 1, "Integer/Double -> Number (least upper bound)");

        Integer boxed = cond(true) ? 1 : Integer.valueOf(2);
        check(boxed == 1, "int/Integer -> int (unboxing)");

        Object nil = cond(true) ? null : "x";
        check(nil == null, "null/String");
        String sn = cond(true) ? null : null;
        check(sn == null, "null/null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
