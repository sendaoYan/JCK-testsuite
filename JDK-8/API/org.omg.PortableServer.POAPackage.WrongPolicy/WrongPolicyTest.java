/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.WrongPolicy conformance: UserException hierarchy and constructors.
 * @run main WrongPolicyTest
 */

public class WrongPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.POAPackage.WrongPolicy.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.POAPackage.WrongPolicy.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.POAPackage.WrongPolicy.class), "implements IDLEntity");
        org.omg.PortableServer.POAPackage.WrongPolicy e = new org.omg.PortableServer.POAPackage.WrongPolicy();
        org.omg.PortableServer.POAPackage.WrongPolicy em = new org.omg.PortableServer.POAPackage.WrongPolicy("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
