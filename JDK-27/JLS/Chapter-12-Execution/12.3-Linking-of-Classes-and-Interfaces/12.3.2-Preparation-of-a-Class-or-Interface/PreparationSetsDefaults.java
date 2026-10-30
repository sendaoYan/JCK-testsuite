/*
 * @test
 * @summary JLS 12.3.2: preparation initializes static fields to their default
 *          values; explicit initializers run later, during initialization. A class
 *          variable read before its initializer runs observes the preparation
 *          default value.
 * @run main PreparationSetsDefaults
 */

public class PreparationSetsDefaults {
    static int a = PreparationSetsDefaults.b;   // qualified name: b still has default 0
    static int b = 1;

    public static void main(String[] args) {
        if (a != 0) throw new AssertionError("a should see b's default 0, got " + a);
        if (b != 1) throw new AssertionError("b should be 1, got " + b);
        System.out.println("PASS");
    }
}
