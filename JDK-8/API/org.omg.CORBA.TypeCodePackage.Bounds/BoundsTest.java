/*
 * @test
 * @summary org.omg.CORBA.TypeCodePackage.Bounds conformance: UserException hierarchy and constructors.
 * @run main BoundsTest
 */

public class BoundsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CORBA.TypeCodePackage.Bounds.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CORBA.TypeCodePackage.Bounds.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.TypeCodePackage.Bounds.class), "implements IDLEntity");
        org.omg.CORBA.TypeCodePackage.Bounds e = new org.omg.CORBA.TypeCodePackage.Bounds();
        org.omg.CORBA.TypeCodePackage.Bounds em = new org.omg.CORBA.TypeCodePackage.Bounds("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
