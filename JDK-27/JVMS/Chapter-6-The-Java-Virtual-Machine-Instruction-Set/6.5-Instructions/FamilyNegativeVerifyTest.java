/*
 * @test
 * @summary JVMS 6.5 / 4.10: negative coverage for the instruction families not
 *          exercised by OperandStackVerify — load-store, conversion,
 *          comparison, conditional-branch, switch, array-store/length/new,
 *          invoke, return, stack-manipulation, monitor and field-access
 *          operand-type errors and stack underflow must all be rejected with
 *          VerifyError when the class is linked.
 * @compile FamilyNegativeVerify.jasm
 * @run main FamilyNegativeVerifyTest
 */

public class FamilyNegativeVerifyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // One class per verifier failure; each is loaded and expected to fail
    // linking with VerifyError.
    static final String[] BAD_CLASSES = {
        "IloadOnLongLocal", "LstoreIntoInt", "D2iOnLong",
        "FcmplOnLong", "DcmpgUnderflow", "IfAcmpeqOnInt",
        "TableswitchUnderflow", "LookupswitchOnLong",
        "IastoreOnLongArray", "ArraylengthOnInt", "NewarrayLongCount",
        "InvokestaticWrongArg", "LreturnOnInt", "AreturnOnInt",
        "SwapUnderflow", "Dup2Underflow", "MonitorenterOnInt",
        "PutfieldWrongType", "GetfieldOnInt"
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
