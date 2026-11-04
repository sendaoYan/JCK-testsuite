/*
 * @test
 * @summary org.omg.PortableInterceptor.ClientRequestInfoOperations conformance: interface shape.
 * @run main ClientRequestInfoOperationsTest
 */

public class ClientRequestInfoOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ClientRequestInfoOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.RequestInfoOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.RequestInfoOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
