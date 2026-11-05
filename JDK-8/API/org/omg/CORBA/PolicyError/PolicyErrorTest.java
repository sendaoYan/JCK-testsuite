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

        // ---- deepened: three ctors and reason field ----
        org.omg.CORBA.PolicyError e0 = new org.omg.CORBA.PolicyError();
        check(e0.reason == 0, "() reason default 0");
        check(e0.getMessage() == null, "() message null");
        org.omg.CORBA.PolicyError eShort = new org.omg.CORBA.PolicyError((short) 3);
        check(eShort.reason == 3, "(short) reason readback");
        check(eShort.getMessage() == null, "(short) message null");
        org.omg.CORBA.PolicyError eFull = new org.omg.CORBA.PolicyError("bad", (short) 4);
        check(eFull.reason == 4, "(String,short) reason readback");
        check("bad".equals(eFull.getMessage()), "(String,short) message readback");
        check(e0 instanceof org.omg.CORBA.UserException, "UserException family");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.PolicyError.class.getModifiers()),
              "PolicyError is final");
        check(org.omg.CORBA.BAD_POLICY_VALUE.value == 3 && org.omg.CORBA.UNSUPPORTED_POLICY_VALUE.value == 4,
              "reason codes align with policy-value constants");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
