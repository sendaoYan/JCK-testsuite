/*
 * @test
 * @summary JLS 4.12.4 / 8.1.3 (Java SE 8): a final or effectively final local
 *          variable may be referenced from within an anonymous class.
 * @run main EffectivelyFinalAnonymousClass
 */

public class EffectivelyFinalAnonymousClass {
    static int passed = 0;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        final int f = 7;
        int ef = 5;
        Runnable r = new Runnable() {
            public void run() {
                check(f == 7, "anonymous class reads final local");
                check(ef == 5, "anonymous class reads effectively final local");
            }
        };
        r.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
