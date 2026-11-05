/*
 * @test
 * @summary org.omg.DynamicAny._DynValueStub conformance: stub/tie class shape.
 * @run main _DynValueStubTest
 */

public class _DynValueStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny._DynValueStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.DynamicAny.DynValue.class.isAssignableFrom(c), "implements org.omg.DynamicAny.DynValue");
        // ---- deepened (batch C, jdk8u372 probed): 3-id table + offline behavior ----
        org.omg.DynamicAny._DynValueStub dpStub = new org.omg.DynamicAny._DynValueStub();
        check(dpStub instanceof org.omg.DynamicAny.DynValue, "implements DynValue");
        check(dpStub instanceof org.omg.DynamicAny.DynValueCommon, "implements DynValueCommon (middle id)");
        check(dpStub instanceof org.omg.DynamicAny.DynAny, "implements DynAny");
        check(java.util.Arrays.toString(dpStub._ids()).equals(
            "[IDL:omg.org/DynamicAny/DynValue:1.0, IDL:omg.org/DynamicAny/DynValueCommon:1.0, IDL:omg.org/DynamicAny/DynAny:1.0]"),
              "_ids() full 3-element array exact (derived first, base last)");
        try {
            dpStub.current_member_name();
            throw new AssertionError("current_member_name() must throw offline");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "offline current_member_name() -> org.omg.CORBA.BAD_OPERATION");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
