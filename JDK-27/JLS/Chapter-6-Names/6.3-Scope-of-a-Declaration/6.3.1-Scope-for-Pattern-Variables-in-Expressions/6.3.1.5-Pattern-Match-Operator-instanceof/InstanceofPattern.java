/*
 * @test
 * @summary JLS 6.3.1.5: a instanceof p introduces a pattern variable when true.
 *          A pattern variable is not permitted to shadow another local variable.
 * @run main InstanceofPattern
 */

public class InstanceofPattern {
    public static void main(String[] args) {
        Object o = "scoped";
        if (o instanceof String s) {
            if (!s.equals("scoped")) throw new AssertionError("pattern variable");
        } else {
            throw new AssertionError("must match");
        }

        System.out.println("PASS");
    }
}
