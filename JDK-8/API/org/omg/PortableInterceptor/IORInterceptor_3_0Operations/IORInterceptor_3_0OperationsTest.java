/*
 * @test
 * @summary org.omg.PortableInterceptor.IORInterceptor_3_0Operations conformance: interface shape.
 * @run main IORInterceptor_3_0OperationsTest
 */

public class IORInterceptor_3_0OperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.IORInterceptor_3_0Operations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.IORInterceptorOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.IORInterceptorOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
