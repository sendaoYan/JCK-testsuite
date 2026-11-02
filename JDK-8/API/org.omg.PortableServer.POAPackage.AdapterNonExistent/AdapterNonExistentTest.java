/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.AdapterNonExistent conformance: UserException hierarchy and constructors.
 * @run main AdapterNonExistentTest
 */

public class AdapterNonExistentTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.POAPackage.AdapterNonExistent.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.POAPackage.AdapterNonExistent.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.POAPackage.AdapterNonExistent.class), "implements IDLEntity");
        org.omg.PortableServer.POAPackage.AdapterNonExistent e = new org.omg.PortableServer.POAPackage.AdapterNonExistent();
        org.omg.PortableServer.POAPackage.AdapterNonExistent em = new org.omg.PortableServer.POAPackage.AdapterNonExistent("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
