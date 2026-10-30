/*
 * @test
 * @summary JLS 14.11.3: a switch statement whose selector evaluates to null and
 *          which has no case null label throws NullPointerException.
 * @run main SwitchNullSelectorNPE
 */

public class SwitchNullSelectorNPE {
    public static void main(String[] args) {
        String s = null;
        try {
            switch (s) {
                case "x": break;
                default: break;
            }
            throw new AssertionError("expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        System.out.println("PASS");
    }
}
