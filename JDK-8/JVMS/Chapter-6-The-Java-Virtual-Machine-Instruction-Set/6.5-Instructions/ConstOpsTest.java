/*
 * @test
 * @summary JVMS 6.5: constant-loading instructions bipush, sipush, ldc (int and
 *          float), and ldc2_w (long and double).
 * @compile ConstOps.jasm
 * @run main ConstOpsTest
 */

public class ConstOpsTest {
    public static void main(String[] args) {
        if (ConstOps.bipush() != 100) throw new AssertionError("bipush");
        if (ConstOps.sipush() != 30000) throw new AssertionError("sipush");
        if (ConstOps.ldcInt() != 40000) throw new AssertionError("ldc int");
        if (ConstOps.ldcFloat() != 1.5f) throw new AssertionError("ldc float");
        if (ConstOps.ldcLong() != 1234567890123L) throw new AssertionError("ldc2_w long");
        if (ConstOps.ldcDouble() != 2.5) throw new AssertionError("ldc2_w double");

        System.out.println("PASS");
    }
}
