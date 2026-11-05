/*
 * @test
 * @summary org.omg.DynamicAny.DynValueCommon conformance: interface shape.
 * @run main DynValueCommonTest
 */

public class DynValueCommonTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynValueCommon");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynValueCommonOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynValueCommonOperations");
        check(org.omg.DynamicAny.DynAny.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAny");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (batch C, jdk8u372 probed): interface lattice ----
        StringBuilder dpIface = new StringBuilder();
        for (Class<?> i : org.omg.DynamicAny.DynValueCommon.class.getInterfaces()) dpIface.append(i.getSimpleName()).append(",");
        check(dpIface.toString().equals("DynValueCommonOperations,DynAny,IDLEntity,"),
              "direct interfaces exact order: " + dpIface);
        check(org.omg.DynamicAny.DynValueCommon.class.isAssignableFrom(org.omg.DynamicAny.DynValue.class),
              "DynValue is-a DynValueCommon");
        check(org.omg.DynamicAny.DynValueCommon.class.isAssignableFrom(org.omg.DynamicAny.DynValueBox.class),
              "DynValueBox is-a DynValueCommon (common base of both branches)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
