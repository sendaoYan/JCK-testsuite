/*
 * @test
 * @summary org.omg.CORBA.DynAnyPackage.InvalidSeq conformance: UserException hierarchy and constructors.
 * @run main InvalidSeqTest
 */

public class InvalidSeqTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.InvalidSeq.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.InvalidSeq.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.InvalidSeq.class), "implements IDLEntity");
        org.omg.CORBA.DynAnyPackage.InvalidSeq e = new org.omg.CORBA.DynAnyPackage.InvalidSeq();
        org.omg.CORBA.DynAnyPackage.InvalidSeq em = new org.omg.CORBA.DynAnyPackage.InvalidSeq("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");

        // ---- deepened: default state, toString, finality (jdk8u372 measured) ----
        org.omg.CORBA.DynAnyPackage.InvalidSeq d0 = new org.omg.CORBA.DynAnyPackage.InvalidSeq();
        check(d0.getMessage() == null, "InvalidSeq() default message null");
        check(d0.toString().contains("org.omg.CORBA.DynAnyPackage.InvalidSeq"), "InvalidSeq toString contains FQCN");
        org.omg.CORBA.DynAnyPackage.InvalidSeq d1 = new org.omg.CORBA.DynAnyPackage.InvalidSeq("custom");
        check("custom".equals(d1.getMessage()), "InvalidSeq(String) message readback");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.DynAnyPackage.InvalidSeq.class.getModifiers()), "InvalidSeq is final");
        check(org.omg.CORBA.DynAnyPackage.InvalidSeq.class.getConstructors().length == 2, "InvalidSeq exposes exactly () and (String) ctors");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
