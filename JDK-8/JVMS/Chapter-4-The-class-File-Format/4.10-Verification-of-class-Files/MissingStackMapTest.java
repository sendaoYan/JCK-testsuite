/*
 * @test
 * @summary JVMS 4.7.4 / 4.10 (Java SE 8): a class file with a branch but no
 *          StackMapTable attribute cannot be verified by split verification,
 *          which requires a stack map frame at each branch target; loading it
 *          throws VerifyError.
 * @compile MissingStackMap.jcod
 * @run main MissingStackMapTest
 */

public class MissingStackMapTest {
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("MissingStackMap");
            throw new AssertionError("missing StackMapTable not rejected");
        } catch (VerifyError e) {
            // expected per JVMS 4.10 (expecting a stack map frame)
        }

        System.out.println("PASS");
    }
}
