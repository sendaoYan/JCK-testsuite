/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.CannotProceed conformance: UserException hierarchy and constructors.
 * @run main CannotProceedTest
 */

public class CannotProceedTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.CannotProceed.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.CannotProceed.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.CannotProceed.class), "implements IDLEntity");
        org.omg.CosNaming.NamingContextPackage.CannotProceed e = new org.omg.CosNaming.NamingContextPackage.CannotProceed();
        check(e.cxt == null, "cxt default null");
        check(e.rest_of_name == null, "rest_of_name default null");
        org.omg.CosNaming.NamingContextPackage.CannotProceed em = new org.omg.CosNaming.NamingContextPackage.CannotProceed("msg", null, null);
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
