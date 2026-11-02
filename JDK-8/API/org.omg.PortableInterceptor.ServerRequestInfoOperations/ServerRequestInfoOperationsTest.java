/*
 * @test
 * @summary org.omg.PortableInterceptor.ServerRequestInfoOperations conformance: interface shape.
 * @run main ServerRequestInfoOperationsTest
 */

public class ServerRequestInfoOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ServerRequestInfoOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.RequestInfoOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.RequestInfoOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
