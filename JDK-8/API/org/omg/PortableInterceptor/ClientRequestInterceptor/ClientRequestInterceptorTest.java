/*
 * @test
 * @summary org.omg.PortableInterceptor.ClientRequestInterceptor conformance: interface shape.
 * @run main ClientRequestInterceptorTest
 */

public class ClientRequestInterceptorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ClientRequestInterceptor");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.ClientRequestInterceptorOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.ClientRequestInterceptorOperations");
        check(org.omg.PortableInterceptor.Interceptor.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.Interceptor");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
