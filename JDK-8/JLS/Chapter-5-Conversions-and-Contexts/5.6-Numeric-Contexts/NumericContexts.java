/*
 * @test
 * @summary JLS 5.6 (Java SE 8): unary and binary numeric promotion widen operands
 *          to a common type before the operation.
 * @run main NumericContexts
 */

public class NumericContexts {
    public static void main(String[] args) {
        // Binary numeric promotion: int + long -> long.
        long l = 1 + 2L;
        if (l != 3L) throw new AssertionError("int + long -> long");

        // int + float -> float.
        float f = 1 + 2.0f;
        if (f != 3.0f) throw new AssertionError("int + float -> float");

        // char + char -> int.
        int ci = 'A' + 'B';
        if (ci != 131) throw new AssertionError("char + char -> int");

        // Unary numeric promotion: -byte -> int.
        byte b = -1;
        int neg = -b;
        if (neg != 1) throw new AssertionError("unary promotion of byte to int");

        System.out.println("PASS");
    }
}
