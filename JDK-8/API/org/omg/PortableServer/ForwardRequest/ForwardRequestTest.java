/*
 * @test
 * @summary org.omg.PortableServer.ForwardRequest conformance: UserException hierarchy and constructors.
 * @run main ForwardRequestTest
 */

public class ForwardRequestTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.ForwardRequest.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.ForwardRequest.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.ForwardRequest.class), "implements IDLEntity");
        org.omg.PortableServer.ForwardRequest e = new org.omg.PortableServer.ForwardRequest();
        check(e.forward_reference == null, "forward_reference default null");
        org.omg.PortableServer.ForwardRequest em = new org.omg.PortableServer.ForwardRequest("msg", null);
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
