/*
 * @test
 * @summary JVMS 6.5: integer/long bitwise and shift instructions.  ishr is
 *          arithmetic (sign-extending) while iushr/lushr are logical
 *          (zero-extending); shift counts are masked to 5 (int) or 6 (long) bits.
 * @compile BitOps.jasm
 * @run main BitOpsTest
 */

public class BitOpsTest {
    public static void main(String[] args) {
        if (BitOps.iand(0xF0, 0x3C) != 0x30) throw new AssertionError("iand");
        if (BitOps.ior(0xF0, 0x0F) != 0xFF) throw new AssertionError("ior");
        if (BitOps.ixor(0xFF, 0x0F) != 0xF0) throw new AssertionError("ixor");

        if (BitOps.ishr(-8, 1) != -4) throw new AssertionError("ishr arithmetic");
        if (BitOps.iushr(-8, 28) != 0xF) throw new AssertionError("iushr logical");
        if (BitOps.ishr(1, 33) != 0) throw new AssertionError("ishr count mask (1>>1)");

        if (BitOps.ineg(5) != -5) throw new AssertionError("ineg");
        if (BitOps.lneg(5L) != -5L) throw new AssertionError("lneg");
        if (BitOps.lshl(1L, 3) != 8L) throw new AssertionError("lshl");
        if (BitOps.lushr(-1L, 60) != 0xF) throw new AssertionError("lushr logical");

        System.out.println("PASS");
    }
}
