/*
 * @test
 * @summary JVMS 6.5: the full 16-opcode conditional branch matrix — ifeq,
 *          ifne, iflt, ifge, ifgt, ifle, if_icmpeq/ne/lt/ge/gt/le,
 *          if_acmpeq/ne, ifnull/ifnonnull — each taken on its true condition
 *          and skipped on its false condition.
 * @compile CondBranchMatrix.jasm
 * @run main CondBranchMatrixTest
 */

public class CondBranchMatrixTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        // single-operand integer branches
        check(CondBranchMatrix.ifEq(0) == 1 && CondBranchMatrix.ifEq(1) == 0, "ifeq == 0");
        check(CondBranchMatrix.ifNe(1) == 1 && CondBranchMatrix.ifNe(0) == 0, "ifne != 0");
        check(CondBranchMatrix.ifLt(-1) == 1 && CondBranchMatrix.ifLt(0) == 0, "iflt < 0");
        check(CondBranchMatrix.ifGe(0) == 1 && CondBranchMatrix.ifGe(-1) == 0, "ifge >= 0");
        check(CondBranchMatrix.ifGt(1) == 1 && CondBranchMatrix.ifGt(0) == 0, "ifgt > 0");
        check(CondBranchMatrix.ifLe(0) == 1 && CondBranchMatrix.ifLe(1) == 0, "ifle <= 0");

        // two-operand integer branches
        check(CondBranchMatrix.ifIcmpEq(3, 3) == 1 && CondBranchMatrix.ifIcmpEq(3, 4) == 0, "if_icmpeq a==b");
        check(CondBranchMatrix.ifIcmpNe(3, 4) == 1 && CondBranchMatrix.ifIcmpNe(3, 3) == 0, "if_icmpne a!=b");
        check(CondBranchMatrix.ifIcmpLt(3, 4) == 1 && CondBranchMatrix.ifIcmpLt(3, 3) == 0, "if_icmplt a<b");
        check(CondBranchMatrix.ifIcmpGe(4, 4) == 1 && CondBranchMatrix.ifIcmpGe(3, 4) == 0, "if_icmpge a>=b");
        check(CondBranchMatrix.ifIcmpGt(4, 3) == 1 && CondBranchMatrix.ifIcmpGt(4, 4) == 0, "if_icmpgt a>b");
        check(CondBranchMatrix.ifIcmpLe(3, 4) == 1 && CondBranchMatrix.ifIcmpLe(4, 3) == 0, "if_icmple a<=b");

        // reference branches
        Object o = new Object();
        Object same = o;
        Object other = new Object();
        check(CondBranchMatrix.ifAcmpEq(o, same) == 1 && CondBranchMatrix.ifAcmpEq(o, other) == 0, "if_acmpeq same reference");
        check(CondBranchMatrix.ifAcmpNe(o, other) == 1 && CondBranchMatrix.ifAcmpNe(o, same) == 0, "if_acmpne different reference");
        check(CondBranchMatrix.ifNull(null) == 1 && CondBranchMatrix.ifNull(o) == 0, "ifnull null reference");
        check(CondBranchMatrix.ifNonNull(o) == 1 && CondBranchMatrix.ifNonNull(null) == 0, "ifnonnull non-null reference");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
