/*
 * @test
 * @summary org.omg.DynamicAny._DynAnyStub conformance: stub/tie class shape.
 * @run main _DynAnyStubTest
 */

public class _DynAnyStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny._DynAnyStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.DynamicAny.DynAny.class.isAssignableFrom(c), "implements org.omg.DynamicAny.DynAny");
        // ---- deepened (batch C, jdk8u372 probed): root stub ids + offline behavior ----
        org.omg.DynamicAny._DynAnyStub dpStub = new org.omg.DynamicAny._DynAnyStub();
        check(dpStub instanceof org.omg.CORBA.portable.ObjectImpl, "extends ObjectImpl");
        check(dpStub instanceof org.omg.DynamicAny.DynAny, "implements DynAny");
        check(java.util.Arrays.toString(dpStub._ids()).equals("[IDL:omg.org/DynamicAny/DynAny:1.0]"),
              "_ids() full array exact (single root id)");
        try {
            dpStub.type();
            throw new AssertionError("type() must throw offline");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "offline type() -> org.omg.CORBA.BAD_OPERATION (delegate not set)");
        }
        try {
            dpStub.component_count();
            throw new AssertionError("component_count() must throw offline");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "offline component_count() -> org.omg.CORBA.BAD_OPERATION");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
