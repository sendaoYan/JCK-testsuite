/*
 * @test
 * @summary JLS 3.10.3 (Java SE 8): the boolean type has exactly two values,
 *          represented by the literals true and false; a boolean literal is
 *          always of type boolean.
 * @run main BooleanLiterals
 */

public class BooleanLiterals {
    static int passed;
    static void check(boolean c, String what) {
        if (c) { passed++; } else { throw new AssertionError("check failed: " + what); }
    }

    public static void main(String[] args) {
        boolean t = true;
        boolean f = false;
        check(t == true && f == false, "boolean literals hold their values");
        check(t && !f, "true is true and false is false");
        check(((Object) true).getClass() == Boolean.class, "true is of type boolean");
        check(((Object) false).getClass() == Boolean.class, "false is of type boolean");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
