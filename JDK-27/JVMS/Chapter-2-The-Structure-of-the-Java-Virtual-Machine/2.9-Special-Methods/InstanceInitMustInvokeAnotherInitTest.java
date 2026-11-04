/*
 * @test
 * @summary JVMS 2.9.1: an instance initialization method must invoke another
 *          <init> method of the same class or of a direct superclass before it
 *          returns; an <init> that returns without such an invocation is
 *          rejected with VerifyError.
 * @compile InstanceInitMustInvokeAnotherInit.jasm
 * @run main InstanceInitMustInvokeAnotherInitTest
 */

public class InstanceInitMustInvokeAnotherInitTest {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        boolean threw = false;
        try {
            Class<?> c = Class.forName("InstanceInitMustInvokeAnotherInit");
            c.getDeclaredConstructor().newInstance();
        } catch (VerifyError e) {
            threw = true;
        }
        check(threw, "<init> without a super/this <init> call rejected");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
