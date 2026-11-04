/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.AlreadyBound conformance: UserException hierarchy and constructors.
 * @run main AlreadyBoundTest
 */

public class AlreadyBoundTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.AlreadyBound.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.AlreadyBound.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CosNaming.NamingContextPackage.AlreadyBound.class), "implements IDLEntity");
        org.omg.CosNaming.NamingContextPackage.AlreadyBound e = new org.omg.CosNaming.NamingContextPackage.AlreadyBound();
        org.omg.CosNaming.NamingContextPackage.AlreadyBound em = new org.omg.CosNaming.NamingContextPackage.AlreadyBound("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
