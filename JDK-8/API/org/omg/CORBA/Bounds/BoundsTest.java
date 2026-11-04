/*
 * @test
 * @summary org.omg.CORBA.Bounds conformance: UserException hierarchy and constructors.
 * @run main BoundsTest
 */

public class BoundsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CORBA.Bounds.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CORBA.Bounds.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.Bounds.class), "implements IDLEntity");
        org.omg.CORBA.Bounds e = new org.omg.CORBA.Bounds();
        org.omg.CORBA.Bounds em = new org.omg.CORBA.Bounds("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
