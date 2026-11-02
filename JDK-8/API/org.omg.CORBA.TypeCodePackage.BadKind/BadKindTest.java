/*
 * @test
 * @summary org.omg.CORBA.TypeCodePackage.BadKind conformance: UserException hierarchy and constructors.
 * @run main BadKindTest
 */

public class BadKindTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CORBA.TypeCodePackage.BadKind.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CORBA.TypeCodePackage.BadKind.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.TypeCodePackage.BadKind.class), "implements IDLEntity");
        org.omg.CORBA.TypeCodePackage.BadKind e = new org.omg.CORBA.TypeCodePackage.BadKind();
        org.omg.CORBA.TypeCodePackage.BadKind em = new org.omg.CORBA.TypeCodePackage.BadKind("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
