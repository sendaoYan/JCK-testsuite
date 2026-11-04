/*
 * @test
 * @summary JVMS 6.5: illegal method invocations fail during resolution with
 *          IncompatibleClassChangeError (invokeinterface on a class),
 *          AbstractMethodError (invokevirtual on an unimplemented abstract
 *          method), and NoSuchMethodError (invokestatic to a missing method).
 * @compile InvokeHelper.java
 * @compile InvokeErrors.jasm
 * @run main InvokeErrorsTest
 */

public class InvokeErrorsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        try {
            InvokeErrors.invokeIfaceOnClass(new Target());
            throw new AssertionError("invokeinterface on class did not throw");
        } catch (IncompatibleClassChangeError e) { }
        check(true, "IncompatibleClassChangeError");

        try {
            InvokeErrors.invokeAbstract(InvokeErrors.newBogus());
            throw new AssertionError("invokevirtual abstract did not throw");
        } catch (AbstractMethodError e) { }
        check(true, "AbstractMethodError");

        try {
            InvokeErrors.noSuchMethod();
            throw new AssertionError("invokestatic missing method did not throw");
        } catch (NoSuchMethodError e) { }
        check(true, "NoSuchMethodError");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
