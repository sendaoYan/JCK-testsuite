/*
 * @test
 * @summary JVMS 2.3.3: the returnAddress type is manipulated only by jsr/ret,
 *          which are not permitted in class files of version 51.0 or later, so
 *          a method containing jsr is rejected with VerifyError when linked.
 * @compile ReturnAddressType.jasm
 * @run main ReturnAddressTypeTest
 */

public class ReturnAddressTypeTest {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        boolean threw = false;
        try {
            Class<?> c = Class.forName("ReturnAddressType");
            c.getMethod("jsrReturnAddress").invoke(null);
        } catch (VerifyError e) {
            threw = true;
        }
        check(threw, "jsr/ret (returnAddress) rejected with VerifyError");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
