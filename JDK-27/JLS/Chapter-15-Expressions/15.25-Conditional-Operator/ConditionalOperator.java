/*
 * @test
 * @summary JLS 15.25: the conditional operator ? : evaluates only the chosen
 *          operand; its type is determined by the numeric promotion or common
 *          reference supertype of the operands.
 * @run main ConditionalOperator
 */

public class ConditionalOperator {
    public static void main(String[] args) {
        if ((true ? 1 : 2) != 1) throw new AssertionError("true branch");
        if ((false ? 1 : 2) != 2) throw new AssertionError("false branch");

        int side = 0;
        int c = true ? (side = 1) : (side = 2);   // only then-branch evaluated
        if (c != 1 || side != 1) throw new AssertionError("only chosen operand evaluated");

        double d = true ? 1 : 2.5;                 // numeric promotion to double
        if (d != 1.0) throw new AssertionError("numeric promotion: " + d);

        Object o = true ? "s" : Integer.valueOf(1);   // lub -> Object
        if (!(o instanceof String)) throw new AssertionError("reference conditional");

        Integer i = false ? Integer.valueOf(1) : null;
        if (i != null) throw new AssertionError("null reference conditional");

        boolean b = true ? true : false;
        if (!b) throw new AssertionError("boolean conditional");

        System.out.println("PASS");
    }
}
