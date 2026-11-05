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

        // ---- deepened: default state, toString, finality (jdk8u372 measured) ----
        org.omg.CORBA.TypeCodePackage.BadKind d0 = new org.omg.CORBA.TypeCodePackage.BadKind();
        check(d0.getMessage() == null, "BadKind() default message null");
        check(d0.toString().contains("org.omg.CORBA.TypeCodePackage.BadKind"), "BadKind toString contains FQCN");
        org.omg.CORBA.TypeCodePackage.BadKind d1 = new org.omg.CORBA.TypeCodePackage.BadKind("custom");
        check("custom".equals(d1.getMessage()), "BadKind(String) message readback");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.TypeCodePackage.BadKind.class.getModifiers()), "BadKind is final");
        check(org.omg.CORBA.TypeCodePackage.BadKind.class.getConstructors().length == 2, "BadKind exposes exactly () and (String) ctors");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
