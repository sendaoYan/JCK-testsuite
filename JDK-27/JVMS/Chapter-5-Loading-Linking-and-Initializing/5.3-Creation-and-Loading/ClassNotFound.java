/*
 * @test
 * @summary JVMS 5.3: attempting to load a nonexistent class via Class.forName
 *          throws ClassNotFoundException.
 * @run main ClassNotFound
 */

public class ClassNotFound {
    public static void main(String[] args) {
        boolean threw = false;
        try {
            Class.forName("nonexistent.pkg.DoesNotExist");
        } catch (ClassNotFoundException e) {
            threw = true;
        }
        if (!threw) throw new AssertionError("no ClassNotFoundException");

        System.out.println("PASS");
    }
}
