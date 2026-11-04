/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.InvalidPolicy conformance: UserException hierarchy and constructors.
 * @run main InvalidPolicyTest
 */

public class InvalidPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.POAPackage.InvalidPolicy.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.POAPackage.InvalidPolicy.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.POAPackage.InvalidPolicy.class), "implements IDLEntity");
        org.omg.PortableServer.POAPackage.InvalidPolicy e = new org.omg.PortableServer.POAPackage.InvalidPolicy();
        check(e.index == (short)0, "index default (short)0");
        org.omg.PortableServer.POAPackage.InvalidPolicy em = new org.omg.PortableServer.POAPackage.InvalidPolicy("msg", (short)0);
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
