/*
 * @test
 * @summary org.omg.CORBA.DynAnyPackage.Invalid conformance: UserException hierarchy and constructors.
 * @run main InvalidTest
 */

public class InvalidTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.Invalid.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.Invalid.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.Invalid.class), "implements IDLEntity");
        org.omg.CORBA.DynAnyPackage.Invalid e = new org.omg.CORBA.DynAnyPackage.Invalid();
        org.omg.CORBA.DynAnyPackage.Invalid em = new org.omg.CORBA.DynAnyPackage.Invalid("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");

        // ---- deepened: default state, toString, finality (jdk8u372 measured) ----
        org.omg.CORBA.DynAnyPackage.Invalid d0 = new org.omg.CORBA.DynAnyPackage.Invalid();
        check(d0.getMessage() == null, "Invalid() default message null");
        check(d0.toString().contains("org.omg.CORBA.DynAnyPackage.Invalid"), "Invalid toString contains FQCN");
        org.omg.CORBA.DynAnyPackage.Invalid d1 = new org.omg.CORBA.DynAnyPackage.Invalid("custom");
        check("custom".equals(d1.getMessage()), "Invalid(String) message readback");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.DynAnyPackage.Invalid.class.getModifiers()), "Invalid is final");
        check(org.omg.CORBA.DynAnyPackage.Invalid.class.getConstructors().length == 2, "Invalid exposes exactly () and (String) ctors");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
