/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.ObjectAlreadyActive conformance: UserException hierarchy and constructors.
 * @run main ObjectAlreadyActiveTest
 */

public class ObjectAlreadyActiveTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.POAPackage.ObjectAlreadyActive.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.POAPackage.ObjectAlreadyActive.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.POAPackage.ObjectAlreadyActive.class), "implements IDLEntity");
        org.omg.PortableServer.POAPackage.ObjectAlreadyActive e = new org.omg.PortableServer.POAPackage.ObjectAlreadyActive();
        org.omg.PortableServer.POAPackage.ObjectAlreadyActive em = new org.omg.PortableServer.POAPackage.ObjectAlreadyActive("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
