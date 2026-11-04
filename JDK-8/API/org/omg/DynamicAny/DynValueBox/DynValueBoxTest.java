/*
 * @test
 * @summary org.omg.DynamicAny.DynValueBox conformance: interface shape.
 * @run main DynValueBoxTest
 */

public class DynValueBoxTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynValueBox");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynValueBoxOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynValueBoxOperations");
        check(org.omg.DynamicAny.DynValueCommon.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynValueCommon");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
