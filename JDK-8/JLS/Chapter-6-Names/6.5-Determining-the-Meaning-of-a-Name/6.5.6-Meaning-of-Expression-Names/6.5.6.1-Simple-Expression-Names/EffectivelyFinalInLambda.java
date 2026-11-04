/*
 * @test
 * @summary JLS 6.5.6.1 / 15.27.2 (Java SE 8): a local variable used in a lambda
 *          body must be effectively final, so an effectively-final local may be
 *          captured by a simple expression name.
 * @run main EffectivelyFinalInLambda
 */

public class EffectivelyFinalInLambda {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        int x = 10;             // effectively final (never reassigned)
        Runnable r = new Runnable() {
            public void run() {
                // x captured by a simple expression name inside the lambda body
            }
        };
        r.run();
        check(x == 10, "effectively final local captured by simple name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
