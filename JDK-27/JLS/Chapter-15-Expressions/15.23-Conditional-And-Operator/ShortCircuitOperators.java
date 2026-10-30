/*
 * @test
 * @summary JLS 15.23 / 15.24: the conditional-and && and conditional-or ||
 *          operators short-circuit, evaluating the right operand only when
 *          necessary.
 * @run main ShortCircuitOperators
 */

public class ShortCircuitOperators {
    static boolean anything() { throw new AssertionError("right operand evaluated"); }

    public static void main(String[] args) {
        int side = 0;
        boolean r = false && ((side = 1) == 1);   // right operand not evaluated
        if (r || side != 0) throw new AssertionError("&& short-circuit");

        boolean s = true || ((side = 2) == 2);    // right operand not evaluated
        if (!s || side != 0) throw new AssertionError("|| short-circuit");

        if (!(true && true)) throw new AssertionError("&& true true");
        if (false && anything()) throw new AssertionError("&& right not evaluated");
        if (!(true || anything())) throw new AssertionError("|| right not evaluated");

        System.out.println("PASS");
    }
}
