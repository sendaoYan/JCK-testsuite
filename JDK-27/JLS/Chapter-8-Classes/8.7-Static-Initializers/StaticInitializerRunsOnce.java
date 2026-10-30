/*
 * @test
 * @summary JLS 8.7: a static initializer runs when the class is initialized, in
 *          source order relative to class variable initializers.
 * @run main StaticInitializerRunsOnce
 */

public class StaticInitializerRunsOnce {
    static int x = 1;
    static { x += 41; }   // runs once at class initialization, in source order

    public static void main(String[] args) {
        if (x != 42) throw new AssertionError("x = " + x);
        // Touching the class again must not re-run the initializer.
        if (StaticInitializerRunsOnce.class == null || x != 42)
            throw new AssertionError("initializer re-ran");
        System.out.println("PASS");
    }
}
