/*
 * @test
 * @summary org.omg.PortableServer.CurrentPackage.NoContext conformance: UserException hierarchy and constructors.
 * @run main NoContextTest
 */

public class NoContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.PortableServer.CurrentPackage.NoContext.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.PortableServer.CurrentPackage.NoContext.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.PortableServer.CurrentPackage.NoContext.class), "implements IDLEntity");
        org.omg.PortableServer.CurrentPackage.NoContext e = new org.omg.PortableServer.CurrentPackage.NoContext();
        org.omg.PortableServer.CurrentPackage.NoContext em = new org.omg.PortableServer.CurrentPackage.NoContext("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
