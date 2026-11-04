/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.ObjectNotActive conformance: UserException hierarchy and constructors.
 * @run main ObjectNotActiveTest
 */

public class ObjectNotActiveTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.POAPackage.ObjectNotActive.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.POAPackage.ObjectNotActive.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.POAPackage.ObjectNotActive.class), "implements IDLEntity");
        org.omg.PortableServer.POAPackage.ObjectNotActive e = new org.omg.PortableServer.POAPackage.ObjectNotActive();
        org.omg.PortableServer.POAPackage.ObjectNotActive em = new org.omg.PortableServer.POAPackage.ObjectNotActive("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
