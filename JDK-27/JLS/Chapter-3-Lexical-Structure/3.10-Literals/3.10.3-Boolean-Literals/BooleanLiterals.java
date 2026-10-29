/*
 * @test
 * @summary JLS 3.10.3: the boolean type has exactly two values, represented by
 *          the literals true and false; a boolean literal is always of type
 *          boolean.
 * @run main BooleanLiterals
 */

public class BooleanLiterals {
    public static void main(String[] args) {
        boolean t = true;
        boolean f = false;
        if (t != true || f != false) {
            throw new AssertionError("boolean literals do not hold their values");
        }
        if (!t || f) {
            throw new AssertionError("boolean literal values are inverted");
        }
        // The literal is of type boolean, not int.
        if (((Object) true).getClass() != Boolean.class) {
            throw new AssertionError("true must be of type boolean");
        }
        if (((Object) false).getClass() != Boolean.class) {
            throw new AssertionError("false must be of type boolean");
        }
        System.out.println("PASS");
    }
}
