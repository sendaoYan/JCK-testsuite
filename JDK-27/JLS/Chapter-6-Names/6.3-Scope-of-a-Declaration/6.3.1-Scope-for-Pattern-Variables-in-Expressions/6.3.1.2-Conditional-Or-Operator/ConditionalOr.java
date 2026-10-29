/*
 * @test
 * @summary JLS 6.3.1.2: for a || b, a pattern variable introduced by a when false
 *          is definitely matched at b, so it is in scope in b.
 * @run main ConditionalOr
 */

public class ConditionalOr {
    static boolean check(Object o) {
        // When o is a String the right operand runs with s matched; when o is
        // not a String the left operand is true and short-circuits.
        return !(o instanceof String s) || s.length() >= 0;
    }
    public static void main(String[] args) {
        if (!check("abc")) throw new AssertionError("string case");
        if (!check(42)) throw new AssertionError("non-string case");

        System.out.println("PASS");
    }
}
