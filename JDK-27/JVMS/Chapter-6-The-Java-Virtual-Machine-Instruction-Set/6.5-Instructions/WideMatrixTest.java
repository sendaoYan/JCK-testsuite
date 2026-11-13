/*
 * @test
 * @summary JVMS 6.5 "wide": the full matrix — every wide-able load/store plus
 *          iinc at the top of the 16-bit local-index range (65534 / 65533), and
 *          the illegal forms that the verifier must reject: "wide" preceding a
 *          non-wideable opcode (iadd, iload_0, nop) and a wide iinc whose
 *          constant operand is truncated.
 * @compile WideMatrix.jasm
 * @run main WideMatrixTest
 */

public class WideMatrixTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final String[] BAD_CLASSES = {
        "WideIaddBad", "WideIload0Bad", "WideNopBad", "WideIincTruncBad"
    };

    public static void main(String[] args) {
        check(WideMatrix.wideIntHi(1234) == 1234, "wide int store/load index 65534");
        check(WideMatrix.wideLongHi(123456789012345L) == 123456789012345L, "wide long store/load index 65533");
        check(WideMatrix.wideFloatHi(3.5f) == 3.5f, "wide float store/load index 65534");
        check(WideMatrix.wideDoubleHi(-0.25d) == -0.25d, "wide double store/load index 65533");
        Object o = new Object();
        check(WideMatrix.wideRefHi(o) == o, "wide ref store/load index 65534");
        check(WideMatrix.wideIincHi(10) == 17, "wide iinc index 65534 by +7");

        for (String name : BAD_CLASSES) {
            try {
                Class.forName(name);
                throw new AssertionError(name + ": no VerifyError thrown");
            } catch (VerifyError e) {
                check(true, name + ": VerifyError");
            } catch (AssertionError e) {
                throw e;
            } catch (Throwable t) {
                throw new AssertionError(name + ": expected VerifyError but got "
                        + t.getClass().getName() + ": " + t.getMessage(), t);
            }
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
