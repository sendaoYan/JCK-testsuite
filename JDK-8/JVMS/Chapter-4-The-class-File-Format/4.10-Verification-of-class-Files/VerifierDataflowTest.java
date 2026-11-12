/*
 * @test
 * @summary JVMS 4.10: the verifier must reject uses of uninitialized values —
 *          an uninitialized `this` in a constructor, an object created by `new`
 *          before its <init> runs, and an unassigned local variable.
 * @compile VerifierDataflow.jasm
 * @run main VerifierDataflowTest
 */

public class VerifierDataflowTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final String[] BAD_CLASSES = {
        "UninitThisGetfield", "UninitObjectGetfield", "UninitLocalRead"
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
