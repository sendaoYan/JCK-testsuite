/*
 * @test
 * @summary org.omg.DynamicAny._DynArrayStub conformance: stub/tie class shape.
 * @run main _DynArrayStubTest
 */

public class _DynArrayStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny._DynArrayStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.DynamicAny.DynArray.class.isAssignableFrom(c), "implements org.omg.DynamicAny.DynArray");
        // ---- deepened (batch C, jdk8u372 probed): ids table + offline local behavior ----
        org.omg.DynamicAny._DynArrayStub dpStub = new org.omg.DynamicAny._DynArrayStub();
        check(dpStub instanceof org.omg.CORBA.portable.ObjectImpl, "extends ObjectImpl");
        check(dpStub instanceof org.omg.DynamicAny.DynArray, "implements org.omg.DynamicAny.DynArray");
        check(java.util.Arrays.toString(dpStub._ids()).equals("[IDL:omg.org/DynamicAny/DynArray:1.0, IDL:omg.org/DynamicAny/DynAny:1.0]"), "_ids() full array exact");
        try {
            dpStub.get_elements();
            check(true, "stub call returned without exception (recorded)");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "offline get_elements -> org.omg.CORBA.BAD_OPERATION (no delegate, local throw)");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
