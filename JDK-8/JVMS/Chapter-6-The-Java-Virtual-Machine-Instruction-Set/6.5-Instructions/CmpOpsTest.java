/*
 * @test
 * @summary JVMS 6.5: lcmp, fcmpl/fcmpg, dcmpl/dcmpg, and ldc.  The cmp
 *          instructions return -1/0/1; the fcmp/dcmp variants differ only when
 *          an operand is NaN (fcmpl/dcmpl -> -1, fcmpg/dcmpg -> +1).
 * @compile CmpOps.jasm
 * @run main CmpOpsTest
 */

public class CmpOpsTest {
    public static void main(String[] args) {
        if (CmpOps.lcmp(3L, 5L) != -1) throw new AssertionError("lcmp <");
        if (CmpOps.lcmp(5L, 3L) != 1) throw new AssertionError("lcmp >");
        if (CmpOps.lcmp(5L, 5L) != 0) throw new AssertionError("lcmp ==");

        if (CmpOps.fcmpl(1.0f, 2.0f) != -1) throw new AssertionError("fcmpl <");
        if (CmpOps.fcmpl(2.0f, 1.0f) != 1) throw new AssertionError("fcmpl >");
        if (CmpOps.fcmpl(2.0f, 2.0f) != 0) throw new AssertionError("fcmpl ==");
        if (CmpOps.fcmpl(Float.NaN, 1.0f) != -1) throw new AssertionError("fcmpl NaN");
        if (CmpOps.fcmpg(Float.NaN, 1.0f) != 1) throw new AssertionError("fcmpg NaN");

        if (CmpOps.dcmpl(1.0, 2.0) != -1) throw new AssertionError("dcmpl <");
        if (CmpOps.dcmpl(2.0, 1.0) != 1) throw new AssertionError("dcmpl >");
        if (CmpOps.dcmpl(Double.NaN, 1.0) != -1) throw new AssertionError("dcmpl NaN");
        if (CmpOps.dcmpg(Double.NaN, 1.0) != 1) throw new AssertionError("dcmpg NaN");

        if (!CmpOps.ldStr().equals("hi")) throw new AssertionError("ldc String");

        System.out.println("PASS");
    }
}
