/*
 * @test
 * @summary JVMS 6.5: ifle/ifgt/if_icmple/if_icmpgt branch when the comparison
 *          holds, and goto_w performs a 4-byte-offset unconditional branch.
 * @compile BranchEdgeOps.jasm
 * @run main BranchEdgeOpsTest
 */

public class BranchEdgeOpsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(BranchEdgeOps.ifle(0) == 1 && BranchEdgeOps.ifle(-5) == 1, "ifle <= 0");
        check(BranchEdgeOps.ifle(5) == 0, "ifle not for > 0");

        check(BranchEdgeOps.ifgt(5) == 1 && BranchEdgeOps.ifgt(0) == 0, "ifgt > 0");

        check(BranchEdgeOps.ifIcmple(3, 5) == 1 && BranchEdgeOps.ifIcmple(5, 5) == 1, "if_icmple <= ");
        check(BranchEdgeOps.ifIcmple(5, 3) == 0, "if_icmple not for >");

        check(BranchEdgeOps.ifIcmpgt(5, 3) == 1 && BranchEdgeOps.ifIcmpgt(5, 5) == 0, "if_icmpgt >");

        check(BranchEdgeOps.gotoW() == 7, "goto_w");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
