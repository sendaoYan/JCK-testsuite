/*
 * @test
 * @summary org.omg.CORBA.DynAnyPackage.InvalidValue conformance: UserException hierarchy and constructors.
 * @run main InvalidValueTest
 */

public class InvalidValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.InvalidValue.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.InvalidValue.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.InvalidValue.class), "implements IDLEntity");
        org.omg.CORBA.DynAnyPackage.InvalidValue e = new org.omg.CORBA.DynAnyPackage.InvalidValue();
        org.omg.CORBA.DynAnyPackage.InvalidValue em = new org.omg.CORBA.DynAnyPackage.InvalidValue("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");

        // ---- deepened: default state, toString, finality (jdk8u372 measured) ----
        org.omg.CORBA.DynAnyPackage.InvalidValue d0 = new org.omg.CORBA.DynAnyPackage.InvalidValue();
        check(d0.getMessage() == null, "InvalidValue() default message null");
        check(d0.toString().contains("org.omg.CORBA.DynAnyPackage.InvalidValue"), "InvalidValue toString contains FQCN");
        org.omg.CORBA.DynAnyPackage.InvalidValue d1 = new org.omg.CORBA.DynAnyPackage.InvalidValue("custom");
        check("custom".equals(d1.getMessage()), "InvalidValue(String) message readback");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.DynAnyPackage.InvalidValue.class.getModifiers()), "InvalidValue is final");
        check(org.omg.CORBA.DynAnyPackage.InvalidValue.class.getConstructors().length == 2, "InvalidValue exposes exactly () and (String) ctors");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
