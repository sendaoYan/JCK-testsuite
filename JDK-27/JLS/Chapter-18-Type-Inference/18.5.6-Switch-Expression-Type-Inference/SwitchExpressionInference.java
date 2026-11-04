/*
 * @test
 * @summary JLS 15.28 / 18.5.6: a switch expression is a poly expression; its type
 *          is inferred from the target type, so each arm is compatible with that
 *          type.
 * @run main SwitchExpressionInference
 */

public class SwitchExpressionInference {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Number n = switch (1) { case 1 -> 1; default -> 2; };   // int arms boxed to Number
        check(n.intValue() == 1, "switch expression target-typed to Number");

        int i = switch (2) { case 2 -> 20; default -> 30; };    // target-typed to int
        check(i == 20, "switch expression target-typed to int");

        String s = switch (1) { case 1 -> "a"; default -> "b"; };
        check("a".equals(s), "switch expression target-typed to String");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
