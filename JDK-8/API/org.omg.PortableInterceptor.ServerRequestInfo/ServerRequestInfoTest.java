/*
 * @test
 * @summary org.omg.PortableInterceptor.ServerRequestInfo conformance: interface shape.
 * @run main ServerRequestInfoTest
 */

public class ServerRequestInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ServerRequestInfo");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.ServerRequestInfoOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.ServerRequestInfoOperations");
        check(org.omg.PortableInterceptor.RequestInfo.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.RequestInfo");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
