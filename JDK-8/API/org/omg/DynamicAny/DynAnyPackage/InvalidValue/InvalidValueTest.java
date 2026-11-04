/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyPackage.InvalidValue conformance: UserException hierarchy and constructors.
 * @run main InvalidValueTest
 */

public class InvalidValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.DynamicAny.DynAnyPackage.InvalidValue.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.DynamicAny.DynAnyPackage.InvalidValue.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.DynamicAny.DynAnyPackage.InvalidValue.class), "implements IDLEntity");
        org.omg.DynamicAny.DynAnyPackage.InvalidValue e = new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        org.omg.DynamicAny.DynAnyPackage.InvalidValue em = new org.omg.DynamicAny.DynAnyPackage.InvalidValue("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
