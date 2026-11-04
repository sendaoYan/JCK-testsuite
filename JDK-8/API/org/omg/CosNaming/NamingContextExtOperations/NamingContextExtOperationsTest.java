/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtOperations conformance: interface shape.
 * @run main NamingContextExtOperationsTest
 */

public class NamingContextExtOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming.NamingContextExtOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CosNaming.NamingContextOperations.class.isAssignableFrom(c), "extends org.omg.CosNaming.NamingContextOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
