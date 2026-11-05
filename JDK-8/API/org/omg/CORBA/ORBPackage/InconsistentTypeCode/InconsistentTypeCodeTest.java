/*
 * @test
 * @summary org.omg.CORBA.ORBPackage.InconsistentTypeCode conformance: UserException hierarchy and constructors.
 * @run main InconsistentTypeCodeTest
 */

public class InconsistentTypeCodeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CORBA.ORBPackage.InconsistentTypeCode.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CORBA.ORBPackage.InconsistentTypeCode.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.ORBPackage.InconsistentTypeCode.class), "implements IDLEntity");
        org.omg.CORBA.ORBPackage.InconsistentTypeCode e = new org.omg.CORBA.ORBPackage.InconsistentTypeCode();
        org.omg.CORBA.ORBPackage.InconsistentTypeCode em = new org.omg.CORBA.ORBPackage.InconsistentTypeCode("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");

        // ---- deepened: default state, toString, finality (jdk8u372 measured) ----
        org.omg.CORBA.ORBPackage.InconsistentTypeCode d0 = new org.omg.CORBA.ORBPackage.InconsistentTypeCode();
        check(d0.getMessage() == null, "InconsistentTypeCode() default message null");
        check(d0.toString().contains("org.omg.CORBA.ORBPackage.InconsistentTypeCode"), "InconsistentTypeCode toString contains FQCN");
        org.omg.CORBA.ORBPackage.InconsistentTypeCode d1 = new org.omg.CORBA.ORBPackage.InconsistentTypeCode("custom");
        check("custom".equals(d1.getMessage()), "InconsistentTypeCode(String) message readback");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.ORBPackage.InconsistentTypeCode.class.getModifiers()), "InconsistentTypeCode is final");
        check(org.omg.CORBA.ORBPackage.InconsistentTypeCode.class.getConstructors().length == 2, "InconsistentTypeCode exposes exactly () and (String) ctors");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
