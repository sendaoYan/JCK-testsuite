/*
 * @test
 * @summary org.omg.CORBA.ORBPackage.InvalidName conformance: UserException hierarchy and constructors.
 * @run main InvalidNameTest
 */

public class InvalidNameTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CORBA.ORBPackage.InvalidName.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CORBA.ORBPackage.InvalidName.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.ORBPackage.InvalidName.class), "implements IDLEntity");
        org.omg.CORBA.ORBPackage.InvalidName e = new org.omg.CORBA.ORBPackage.InvalidName();
        org.omg.CORBA.ORBPackage.InvalidName em = new org.omg.CORBA.ORBPackage.InvalidName("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");

        // ---- deepened: default state, toString, finality (jdk8u372 measured) ----
        org.omg.CORBA.ORBPackage.InvalidName d0 = new org.omg.CORBA.ORBPackage.InvalidName();
        check(d0.getMessage() == null, "InvalidName() default message null");
        check(d0.toString().contains("org.omg.CORBA.ORBPackage.InvalidName"), "InvalidName toString contains FQCN");
        org.omg.CORBA.ORBPackage.InvalidName d1 = new org.omg.CORBA.ORBPackage.InvalidName("custom");
        check("custom".equals(d1.getMessage()), "InvalidName(String) message readback");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.ORBPackage.InvalidName.class.getModifiers()), "InvalidName is final");
        check(org.omg.CORBA.ORBPackage.InvalidName.class.getConstructors().length == 2, "InvalidName exposes exactly () and (String) ctors");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
