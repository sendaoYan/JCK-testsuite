/*
 * @test
 * @summary JVMS 6.5: fcmpl/fcmpg/dcmpl/dcmpg return -1/+1 when either operand
 *          is NaN (second operand here), treat signed zero as equal, and lcmp
 *          orders the long extremes correctly.
 * @compile CmpNanOps.jasm
 * @run main CmpNanOpsTest
 */

public class CmpNanOpsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(CmpNanOps.fcmplNan2(1.0f, Float.NaN) == -1, "fcmpl NaN second -> -1");
        check(CmpNanOps.fcmpgNan2(1.0f, Float.NaN) == 1, "fcmpg NaN second -> +1");
        check(CmpNanOps.dcmplNan2(1.0, Double.NaN) == -1, "dcmpl NaN second -> -1");
        check(CmpNanOps.dcmpgNan2(1.0, Double.NaN) == 1, "dcmpg NaN second -> +1");

        check(CmpNanOps.fcmplZero() == 0, "fcmpl -0.0 == +0.0");
        check(CmpNanOps.dcmpgZero() == 0, "dcmpg -0.0 == +0.0");

        check(CmpNanOps.lcmpMinMax() == -1, "lcmp MIN < MAX");
        check(CmpNanOps.lcmpMaxMin() == 1, "lcmp MAX > MIN");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
