/*
 * @test
 * @summary JVMS 6.5: iinc, multianewarray, baload/caload/saload and
 *          bastore/castore/sastore, plus the dup_x1 and dup_x2 stack
 *          manipulation forms.
 * @compile MiscOps.jasm
 * @run main MiscOpsTest
 */

public class MiscOpsTest {
    public static void main(String[] args) {
        if (MiscOps.iincTest() != 5) throw new AssertionError("iinc");

        int[][] m = MiscOps.multiArray(2, 3);
        if (m.length != 2 || m[0].length != 3 || m[1].length != 3)
            throw new AssertionError("multianewarray");

        byte[] ba = { 1, -2, 3 };
        if (MiscOps.baload(ba, 1) != -2) throw new AssertionError("baload");
        MiscOps.bastore(ba, 0, (byte) 9);
        if (ba[0] != 9) throw new AssertionError("bastore");

        char[] ca = { 'a', 'b' };
        if (MiscOps.caload(ca, 1) != 'b') throw new AssertionError("caload");
        MiscOps.castore(ca, 0, 'z');
        if (ca[0] != 'z') throw new AssertionError("castore");

        short[] sa = { 5, -6 };
        if (MiscOps.saload(sa, 1) != -6) throw new AssertionError("saload");
        MiscOps.sastore(sa, 0, (short) 42);
        if (sa[0] != 42) throw new AssertionError("sastore");

        if (MiscOps.dupX1() != 5) throw new AssertionError("dup_x1");
        if (MiscOps.dupX2() != 9) throw new AssertionError("dup_x2");

        System.out.println("PASS");
    }
}
