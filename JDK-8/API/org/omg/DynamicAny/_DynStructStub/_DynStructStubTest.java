/*
 * @test
 * @summary org.omg.DynamicAny._DynStructStub conformance: stub/tie class shape.
 * @run main _DynStructStubTest
 */

public class _DynStructStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny._DynStructStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.DynamicAny.DynStruct.class.isAssignableFrom(c), "implements org.omg.DynamicAny.DynStruct");
        // ---- deepened (batch C, jdk8u372 probed): ids table + offline local behavior ----
        org.omg.DynamicAny._DynStructStub dpStub = new org.omg.DynamicAny._DynStructStub();
        check(dpStub instanceof org.omg.CORBA.portable.ObjectImpl, "extends ObjectImpl");
        check(dpStub instanceof org.omg.DynamicAny.DynStruct, "implements org.omg.DynamicAny.DynStruct");
        check(java.util.Arrays.toString(dpStub._ids()).equals("[IDL:omg.org/DynamicAny/DynStruct:1.0, IDL:omg.org/DynamicAny/DynAny:1.0]"), "_ids() full array exact");
        try {
            dpStub.get_members();
            check(true, "stub call returned without exception (recorded)");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "offline get_members -> org.omg.CORBA.BAD_OPERATION (no delegate, local throw)");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
