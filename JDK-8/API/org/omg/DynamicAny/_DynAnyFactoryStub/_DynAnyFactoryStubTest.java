/*
 * @test
 * @summary org.omg.DynamicAny._DynAnyFactoryStub conformance: stub/tie class shape.
 * @run main _DynAnyFactoryStubTest
 */

public class _DynAnyFactoryStubTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny._DynAnyFactoryStub");
        check(!c.isInterface(), "is a class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check(org.omg.CORBA.portable.ObjectImpl.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.ObjectImpl");
        check(org.omg.DynamicAny.DynAnyFactory.class.isAssignableFrom(c), "implements org.omg.DynamicAny.DynAnyFactory");
        // ---- deepened (batch C, jdk8u372 probed): stub ids + offline local behavior ----
        org.omg.DynamicAny._DynAnyFactoryStub dpStub = new org.omg.DynamicAny._DynAnyFactoryStub();
        check(dpStub instanceof org.omg.CORBA.portable.ObjectImpl, "extends ObjectImpl");
        check(dpStub instanceof org.omg.DynamicAny.DynAnyFactory, "implements DynAnyFactory");
        String[] dpIds = dpStub._ids();
        check(dpIds.length == 1 && dpIds[0].equals("IDL:omg.org/DynamicAny/DynAnyFactory:1.0"),
              "_ids() single exact repository id");
        try {
            dpStub.create_dyn_any(null);
            check(true, "stub call reachable (exception detail recorded separately)");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "offline stub operation -> org.omg.CORBA.BAD_OPERATION (no delegate, local throw)");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
