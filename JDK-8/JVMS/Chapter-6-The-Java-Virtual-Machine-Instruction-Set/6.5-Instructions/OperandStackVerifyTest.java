/*
 * @test
 * @summary JVMS 6.5 / 4.10: operand-stack type mismatch, underflow and
 *          max_stack overflow must all be rejected with VerifyError.
 * @compile OperandStackVerify.jasm
 * @run main OperandStackVerifyTest
 */

public class OperandStackVerifyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // One class per verifier failure; each is loaded and expected to fail
    // linking with VerifyError.
    static final String[] BAD_CLASSES = {
        "IaddLongOperand", "LaddIntOperand", "FaddDoubleOperand",
        "LcmpIntOperand", "I2lLongOperand", "IshllongOperand",
        "IfeqLongOperand", "IfnullIntOperand", "IfIcmpeqUnderflow",
        "IaloadOnLongArray", "AaloadOnIntArray", "IreturnLong",
        "IaddUnderflow", "LaddUnderflow", "IreturnUnderflow",
        "DupUnderflow", "MaxStackLongOverflow"
    };

    public static void main(String[] args) throws Exception {
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
