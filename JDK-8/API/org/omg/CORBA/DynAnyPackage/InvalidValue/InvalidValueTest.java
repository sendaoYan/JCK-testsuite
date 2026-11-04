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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
