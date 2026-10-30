/*
 * @test
 * @summary JVMS 4.7.4 / 4.10 (Java SE 8): a StackMapTable frame whose
 *          verification type conflicts with the actual locals at the branch
 *          target (frame says Object, code has int) is rejected by the verifier
 *          with VerifyError.
 * @compile InconsistentStackMap.jcod
 * @run main InconsistentStackMapTest
 */

public class InconsistentStackMapTest {
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("InconsistentStackMap");
            throw new AssertionError("inconsistent StackMapTable frame not rejected");
        } catch (VerifyError e) {
            // expected per JVMS 4.10 (inconsistent stack map frame)
        }

        System.out.println("PASS");
    }
}
