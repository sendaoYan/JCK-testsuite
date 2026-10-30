/*
 * @test
 * @summary JVMS 6.5: conditional branch instructions (ifeq, ifne, iflt, ifge,
 *          ifnull, ifnonnull, if_icmplt, if_icmpge, if_acmpeq, if_acmpne)
 *          branch when their comparison holds.
 * @compile CondBranches.jasm
 * @run main CondBranchesTest
 */

public class CondBranchesTest {
    public static void main(String[] args) {
        if (CondBranches.ifeq(0) != 1 || CondBranches.ifeq(5) != 0) throw new AssertionError("ifeq");
        if (CondBranches.ifne(5) != 1 || CondBranches.ifne(0) != 0) throw new AssertionError("ifne");
        if (CondBranches.iflt(-1) != 1 || CondBranches.iflt(1) != 0) throw new AssertionError("iflt");
        if (CondBranches.ifge(0) != 1 || CondBranches.ifge(-1) != 0) throw new AssertionError("ifge");

        if (CondBranches.ifnull(null) != 1 || CondBranches.ifnull("x") != 0) throw new AssertionError("ifnull");
        if (CondBranches.ifnonnull("x") != 1 || CondBranches.ifnonnull(null) != 0) throw new AssertionError("ifnonnull");

        if (CondBranches.ifIcmplt(3, 5) != 1 || CondBranches.ifIcmplt(5, 3) != 0) throw new AssertionError("if_icmplt");
        if (CondBranches.ifIcmpge(5, 5) != 1 || CondBranches.ifIcmpge(3, 5) != 0) throw new AssertionError("if_icmpge");

        Object a = new Object(), b = new Object();
        if (CondBranches.ifAcmeq(a, a) != 1 || CondBranches.ifAcmeq(a, b) != 0) throw new AssertionError("if_acmpeq");
        if (CondBranches.ifAcmpne(a, b) != 1 || CondBranches.ifAcmpne(a, a) != 0) throw new AssertionError("if_acmpne");

        System.out.println("PASS");
    }
}
