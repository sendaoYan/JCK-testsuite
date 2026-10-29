/*
 * @test
 * @summary JLS 6.5.6.1 Example 6.5.6.1-3: an effectively final local variable
 *          may be referenced from a lambda expression or inner class.
 * @run main EffectivelyFinalInLambda
 */

public class EffectivelyFinalInLambda {
    public static void main(String[] args) {
        final int c = 3;
        Runnable r = () -> {
            if (c != 3) throw new AssertionError("c");
        };
        r.run();

        System.out.println("PASS");
    }
}
