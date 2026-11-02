/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.NotEmpty conformance: UserException hierarchy and constructors.
 * @run main NotEmptyTest
 */

public class NotEmptyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.NotEmpty.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.NotEmpty.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.NotEmpty.class), "implements IDLEntity");
        org.omg.CosNaming.NamingContextPackage.NotEmpty e = new org.omg.CosNaming.NamingContextPackage.NotEmpty();
        org.omg.CosNaming.NamingContextPackage.NotEmpty em = new org.omg.CosNaming.NamingContextPackage.NotEmpty("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
