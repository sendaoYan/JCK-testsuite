/*
 * @test
 * @summary JLS 15.18: additive operators; string concatenation converts
 *          non-String operands left-to-right, with null becoming "null".
 * @run main AdditiveOperators
 */

public class AdditiveOperators {
    public static void main(String[] args) {
        if (1 + 2 != 3) throw new AssertionError("+");
        if (5 - 3 != 2) throw new AssertionError("-");

        if (!"ab".equals("a" + "b")) throw new AssertionError("string concat");
        if (!"x12".equals("x" + 1 + 2)) throw new AssertionError("left-to-right concat");
        if (!"3x".equals(1 + 2 + "x")) throw new AssertionError("numeric then concat");
        if (!"v=null".equals("v=" + null)) throw new AssertionError("null concat");
        if (!"1.5".equals("" + 1.5)) throw new AssertionError("double concat");

        System.out.println("PASS");
    }
}
