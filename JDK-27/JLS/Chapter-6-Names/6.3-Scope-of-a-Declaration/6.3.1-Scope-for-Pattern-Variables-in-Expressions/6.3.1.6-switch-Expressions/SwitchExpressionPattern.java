/*
 * @test
 * @summary JLS 6.3.1.6: in a switch expression with switch rules, a pattern
 *          variable introduced by a switch label is definitely matched in the
 *          associated switch rule expression, block, or throw statement.
 * @run main SwitchExpressionPattern
 */

public class SwitchExpressionPattern {
    static int len(Object o) {
        return switch (o) {
            case String s -> s.length();
            case Integer i -> i;
            default -> -1;
        };
    }
    public static void main(String[] args) {
        if (len("abc") != 3) throw new AssertionError("String");
        if (len(Integer.valueOf(7)) != 7) throw new AssertionError("Integer");
        if (len(3.14) != -1) throw new AssertionError("default");

        System.out.println("PASS");
    }
}
