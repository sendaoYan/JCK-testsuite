/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.ServantAlreadyActive conformance: UserException hierarchy and constructors.
 * @run main ServantAlreadyActiveTest
 */

public class ServantAlreadyActiveTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.POAPackage.ServantAlreadyActive.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.POAPackage.ServantAlreadyActive.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.POAPackage.ServantAlreadyActive.class), "implements IDLEntity");
        org.omg.PortableServer.POAPackage.ServantAlreadyActive e = new org.omg.PortableServer.POAPackage.ServantAlreadyActive();
        org.omg.PortableServer.POAPackage.ServantAlreadyActive em = new org.omg.PortableServer.POAPackage.ServantAlreadyActive("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
