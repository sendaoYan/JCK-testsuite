/*
 * @test
 * @summary org.omg.PortableServer.POAManagerPackage.AdapterInactive conformance: UserException hierarchy and constructors.
 * @run main AdapterInactiveTest
 */

public class AdapterInactiveTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.POAManagerPackage.AdapterInactive.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.POAManagerPackage.AdapterInactive.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.POAManagerPackage.AdapterInactive.class), "implements IDLEntity");
        org.omg.PortableServer.POAManagerPackage.AdapterInactive e = new org.omg.PortableServer.POAManagerPackage.AdapterInactive();
        org.omg.PortableServer.POAManagerPackage.AdapterInactive em = new org.omg.PortableServer.POAManagerPackage.AdapterInactive("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
