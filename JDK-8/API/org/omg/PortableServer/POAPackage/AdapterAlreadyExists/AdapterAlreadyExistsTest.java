/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.AdapterAlreadyExists conformance: UserException hierarchy and constructors.
 * @run main AdapterAlreadyExistsTest
 */

public class AdapterAlreadyExistsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.POAPackage.AdapterAlreadyExists.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.POAPackage.AdapterAlreadyExists.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.POAPackage.AdapterAlreadyExists.class), "implements IDLEntity");
        org.omg.PortableServer.POAPackage.AdapterAlreadyExists e = new org.omg.PortableServer.POAPackage.AdapterAlreadyExists();
        org.omg.PortableServer.POAPackage.AdapterAlreadyExists em = new org.omg.PortableServer.POAPackage.AdapterAlreadyExists("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
