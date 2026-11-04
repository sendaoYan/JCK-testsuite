/*
 * @test
 * @summary org.omg.PortableInterceptor.ServerRequestInterceptor conformance: interface shape.
 * @run main ServerRequestInterceptorTest
 */

public class ServerRequestInterceptorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ServerRequestInterceptor");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.ServerRequestInterceptorOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.ServerRequestInterceptorOperations");
        check(org.omg.PortableInterceptor.Interceptor.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.Interceptor");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
