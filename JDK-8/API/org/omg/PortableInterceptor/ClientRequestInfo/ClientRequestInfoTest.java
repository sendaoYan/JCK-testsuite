/*
 * @test
 * @summary org.omg.PortableInterceptor.ClientRequestInfo conformance: interface shape.
 * @run main ClientRequestInfoTest
 */

public class ClientRequestInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.ClientRequestInfo");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableInterceptor.ClientRequestInfoOperations.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.ClientRequestInfoOperations");
        check(org.omg.PortableInterceptor.RequestInfo.class.isAssignableFrom(c), "extends org.omg.PortableInterceptor.RequestInfo");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
