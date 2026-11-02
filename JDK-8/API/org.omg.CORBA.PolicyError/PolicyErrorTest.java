/*
 * @test
 * @summary org.omg.CORBA.PolicyError conformance: UserException hierarchy and constructors.
 * @run main PolicyErrorTest
 */

public class PolicyErrorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CORBA.PolicyError.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CORBA.PolicyError.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.PolicyError.class), "implements IDLEntity");
        org.omg.CORBA.PolicyError e = new org.omg.CORBA.PolicyError();
        check(e.reason == (short)0, "reason default (short)0");
        org.omg.CORBA.PolicyError em = new org.omg.CORBA.PolicyError("msg", (short)0);
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
