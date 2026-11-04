/*
 * @test
 * @summary org.omg.CosNaming.NamingContextOperations conformance: interface shape.
 * @run main NamingContextOperationsTest
 */

public class NamingContextOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming.NamingContextOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
