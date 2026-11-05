/*
 * @test
 * @summary org.omg.CORBA.DynAnyPackage.TypeMismatch conformance: UserException hierarchy and constructors.
 * @run main TypeMismatchTest
 */

public class TypeMismatchTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.TypeMismatch.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.TypeMismatch.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.DynAnyPackage.TypeMismatch.class), "implements IDLEntity");
        org.omg.CORBA.DynAnyPackage.TypeMismatch e = new org.omg.CORBA.DynAnyPackage.TypeMismatch();
        org.omg.CORBA.DynAnyPackage.TypeMismatch em = new org.omg.CORBA.DynAnyPackage.TypeMismatch("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");

        // ---- deepened: default state, toString, finality (jdk8u372 measured) ----
        org.omg.CORBA.DynAnyPackage.TypeMismatch d0 = new org.omg.CORBA.DynAnyPackage.TypeMismatch();
        check(d0.getMessage() == null, "TypeMismatch() default message null");
        check(d0.toString().contains("org.omg.CORBA.DynAnyPackage.TypeMismatch"), "TypeMismatch toString contains FQCN");
        org.omg.CORBA.DynAnyPackage.TypeMismatch d1 = new org.omg.CORBA.DynAnyPackage.TypeMismatch("custom");
        check("custom".equals(d1.getMessage()), "TypeMismatch(String) message readback");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.DynAnyPackage.TypeMismatch.class.getModifiers()), "TypeMismatch is final");
        check(org.omg.CORBA.DynAnyPackage.TypeMismatch.class.getConstructors().length == 2, "TypeMismatch exposes exactly () and (String) ctors");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
