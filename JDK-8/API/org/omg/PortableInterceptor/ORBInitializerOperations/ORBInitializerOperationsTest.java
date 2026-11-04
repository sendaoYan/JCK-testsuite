/*
 * @test
 * @summary org.omg.PortableInterceptor.ORBInitializerOperations conformance: interface shape.
 * @run main ORBInitializerOperationsTest
 */

public class ORBInitializerOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ORBInitializerOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
