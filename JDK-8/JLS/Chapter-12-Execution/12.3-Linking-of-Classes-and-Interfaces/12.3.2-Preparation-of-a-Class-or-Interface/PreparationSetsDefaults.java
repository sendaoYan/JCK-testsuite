/*
 * @test
 * @summary JLS 12.3.2 (Java SE 8): preparation initializes static fields to their
 *          default values; explicit initializers run later during initialization,
 *          so a qualified read before its initializer runs observes the default.
 * @run main PreparationSetsDefaults
 */

public class PreparationSetsDefaults {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int a = PreparationSetsDefaults.b;
    static int b = 1;

    public static void main(String[] args) {
        check(a == 0, "a should see b's default 0, got " + a);
        check(b == 1, "b should be 1, got " + b);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
