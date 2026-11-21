/*
 * @test
 * @summary JVMS 4.10.1.9: invoking any method on an uninitialized `this`
 *          before the superclass <init> call is rejected with VerifyError.
 * @compile UninitThisInvoke.jasm
 * @run main UninitThisInvokeTest
 */

public class UninitThisInvokeTest {
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("UninitThisInvoke");
            throw new AssertionError("invokevirtual on uninitialized this not rejected");
        } catch (VerifyError e) {
            // expected per JVMS 4.10.1.9 (uninitializedThis use before super)
        }

        System.out.println("PASS");
    }
}
