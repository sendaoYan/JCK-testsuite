/*
 * @test
 * @summary JLS 15.19: shift operators; >> is sign-preserving, >>> is
 *          zero-filling, and the shift distance is masked to the low 5 bits
 *          for int.
 * @run main ShiftOperators
 */

public class ShiftOperators {
    public static void main(String[] args) {
        if (8 << 1 != 16) throw new AssertionError("<<");
        if (8 >> 1 != 4) throw new AssertionError(">>");
        if (-8 >> 1 != -4) throw new AssertionError(">> sign-preserving");
        if (-8 >>> 1 != 0x7FFFFFFC) throw new AssertionError(">>> zero-filling");
        if (1 << 32 != 1) throw new AssertionError("shift distance masked (32)");
        if (1 << 33 != 2) throw new AssertionError("shift distance masked (33)");

        System.out.println("PASS");
    }
}
