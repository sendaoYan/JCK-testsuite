/*
 * @test
 * @summary JLS 5.1.1 (Java SE 8): a conversion from a type to that same type is
 *          permitted for any type. It is always permitted for an expression to
 *          have the desired type to begin with, and redundant cast operators
 *          are permitted for clarity.
 * @run main IdentityConversion
 */

public class IdentityConversion {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // Identity conversion: same-type assignment and redundant casts.
        int i = 5;
        int j = i;              // identity
        check(j == 5, "int -> int identity");

        String s = "x";
        String t = (String) s;  // redundant cast is permitted
        check(s == t, "redundant reference cast is identity");

        Object o = new Object();
        Object o2 = (Object) o;
        check(o == o2, "Object -> Object identity");

        double d = 1.5;
        double d2 = (double) d;
        check(d == d2, "double -> double identity");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
