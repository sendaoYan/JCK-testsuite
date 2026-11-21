/*
 * @test
 * @summary JVMS 4.10.1: an object allocated by `new` stays uninitialized until
 *          its <init> runs, even after a store/load round trip, so invoking a
 *          method on it before <init> is rejected with VerifyError.
 * @compile UninitObjectUse.jasm
 * @run main UninitObjectUseTest
 */

public class UninitObjectUseTest {
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("UninitObjectUse");
            throw new AssertionError("use of uninitialized object not rejected");
        } catch (VerifyError e) {
            // expected per JVMS 4.10.1 (uninitialized object used before <init>)
        }

        System.out.println("PASS");
    }
}
