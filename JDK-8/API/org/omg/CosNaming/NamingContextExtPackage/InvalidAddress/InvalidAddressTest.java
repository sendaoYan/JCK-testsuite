/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtPackage.InvalidAddress conformance: UserException hierarchy and constructors.
 * @run main InvalidAddressTest
 */

public class InvalidAddressTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CosNaming.NamingContextExtPackage.InvalidAddress.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CosNaming.NamingContextExtPackage.InvalidAddress.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CosNaming.NamingContextExtPackage.InvalidAddress.class), "implements IDLEntity");
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddress e = new org.omg.CosNaming.NamingContextExtPackage.InvalidAddress();
        org.omg.CosNaming.NamingContextExtPackage.InvalidAddress em = new org.omg.CosNaming.NamingContextExtPackage.InvalidAddress("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
