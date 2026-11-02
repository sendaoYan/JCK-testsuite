/*
 * @test
 * @summary org.omg.PortableInterceptor.ForwardRequest conformance: UserException hierarchy and constructors.
 * @run main ForwardRequestTest
 */

public class ForwardRequestTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableInterceptor.ForwardRequest.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableInterceptor.ForwardRequest.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableInterceptor.ForwardRequest.class), "implements IDLEntity");
        org.omg.PortableInterceptor.ForwardRequest e = new org.omg.PortableInterceptor.ForwardRequest();
        check(e.forward == null, "forward default null");
        org.omg.PortableInterceptor.ForwardRequest em = new org.omg.PortableInterceptor.ForwardRequest("msg", null);
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
