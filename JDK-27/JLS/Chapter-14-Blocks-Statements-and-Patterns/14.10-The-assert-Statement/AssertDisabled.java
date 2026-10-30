/*
 * @test
 * @summary JLS 14.10: a disabled assert statement does nothing; in particular
 *          neither Expression1 nor Expression2 is evaluated.
 * @run main AssertDisabled
 */

public class AssertDisabled {
    static int sideEffects = 0;

    static boolean bump() { sideEffects++; return false; }

    public static void main(String[] args) {
        assert bump();          // Expression1 not evaluated
        assert false : bump();  // neither Expression1 nor Expression2 evaluated
        if (sideEffects != 0)
            throw new AssertionError("disabled assert evaluated an expression: " + sideEffects);
        System.out.println("PASS");
    }
}
