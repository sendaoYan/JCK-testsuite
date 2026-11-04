/*
 * @test
 * @summary JLS 5.6 (Java SE 27): a switch expression whose result expressions
 *          are all convertible to a numeric type is a numeric choice context.
 *          General numeric promotion applies: all-short/byte/char result
 *          expressions promote to short/byte/char when the constants are
 *          representable, otherwise to int; double/float/long dominate.
 * @run main NumericChoiceContextSwitch
 */

public class NumericChoiceContextSwitch {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int nonConst() { return 5; }

    public static void main(String[] args) {
        // All-short result expressions: promoted type is short.
        short s = switch (0) { case 0 -> (short) 1; default -> (short) 2; };
        check(s == 1, "all-short switch result promotes to short");

        // short + byte constants: promoted to short (byte widens to short).
        short sb = switch (0) { case 0 -> (short) 7; default -> (byte) 8; };
        check(sb == 7, "short+byte constants promote to short");

        // All-byte result expressions: promoted type is byte.
        byte b = switch (0) { case 0 -> (byte) 1; default -> (byte) 2; };
        check(b == 1, "all-byte switch result promotes to byte");

        // All-char result expressions: promoted type is char.
        char c = switch (0) { case 0 -> (char) 65; default -> (char) 66; };
        check(c == 'A', "all-char switch result promotes to char");

        // A non-constant int branch forces the promoted type to int.
        int i = switch (0) { case 0 -> (short) 1; default -> nonConst(); };
        check(i == 1, "non-constant int branch promotes result to int");

        // A constant int value representable in short may join short branches.
        short sc = switch (0) { case 0 -> (short) 1; default -> 42; };
        check(sc == 1, "constant int representable in short joins short promotion");

        // double dominates all numeric choice result expressions.
        double d = switch (0) { case 0 -> 1; default -> 2.5; };
        check(d == 1.0, "double branch promotes result to double");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
