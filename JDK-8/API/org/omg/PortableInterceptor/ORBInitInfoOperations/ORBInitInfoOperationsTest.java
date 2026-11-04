/*
 * @test
 * @summary org.omg.PortableInterceptor.ORBInitInfoOperations conformance: interface shape.
 * @run main ORBInitInfoOperationsTest
 */

public class ORBInitInfoOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ORBInitInfoOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
