/*
 * @test
 * @summary JLS 15.20.2: the type comparison operator instanceof tests the
 *          run-time type of a reference; a pattern form also declares a pattern
 *          variable whose scope is governed by flow analysis (definitely
 *          instanceof on the && right / || left).
 * @run main InstanceofPattern
 */

public class InstanceofPattern {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Object o = "hello";
        if (o instanceof String s) {
            check(s.length() == 5, "pattern variable bound to the matched value");
        } else {
            throw new AssertionError("should match String");
        }

        Object n = Integer.valueOf(1);
        if (n instanceof String t) {
            throw new AssertionError("Integer must not match String");
        } else {
            check(true, "no match, pattern variable not bound");
        }

        check(o instanceof String s2 && s2.length() == 5, "pattern variable in scope on && right");
        check(!(o instanceof String s3) || s3.length() == 5, "pattern variable in scope on || right");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
