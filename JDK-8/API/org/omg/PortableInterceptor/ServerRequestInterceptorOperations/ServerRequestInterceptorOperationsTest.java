/*
 * @test
 * @summary org.omg.PortableInterceptor.ServerRequestInterceptorOperations conformance: interface shape.
 * @run main ServerRequestInterceptorOperationsTest
 */

public class ServerRequestInterceptorOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ServerRequestInterceptorOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.InterceptorOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.InterceptorOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
