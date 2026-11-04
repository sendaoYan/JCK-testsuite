/*
 * @test
 * @summary JVMS 6.5: invokespecial resolves a method directly (super or private)
 *          without dynamic dispatch, and invokestatic invokes a static method
 *          after resolution.
 * @compile InvokeHelper.java
 * @compile InvokeSpecialStatic.jasm
 * @run main InvokeSpecialStaticTest
 */

public class InvokeSpecialStaticTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        InvokeSpecialStatic obj = new InvokeSpecialStatic();
        check(obj.who() == 99, "subclass override for comparison");
        check(InvokeSpecialStatic.callSuper(obj) == 1, "invokespecial calls super, not override");
        check(InvokeSpecialStatic.callStatic() == 42, "invokestatic");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
