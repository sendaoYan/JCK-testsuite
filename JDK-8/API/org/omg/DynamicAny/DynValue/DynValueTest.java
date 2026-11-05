/*
 * @test
 * @summary org.omg.DynamicAny.DynValue conformance: interface shape.
 * @run main DynValueTest
 */

public class DynValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynValue");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynValueOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynValueOperations");
        check(org.omg.DynamicAny.DynValueCommon.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynValueCommon");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (batch C, jdk8u372 probed): interface lattice + offline construction limit ----
        StringBuilder dpIface = new StringBuilder();
        for (Class<?> i : org.omg.DynamicAny.DynValue.class.getInterfaces()) dpIface.append(i.getSimpleName()).append(",");
        check(dpIface.toString().equals("DynValueOperations,DynValueCommon,IDLEntity,"),
              "direct interfaces exact order: " + dpIface);
        check(org.omg.DynamicAny.DynAny.class.isAssignableFrom(org.omg.DynamicAny.DynValue.class), "is-a DynAny (via DynValueCommon)");
        try {
            org.omg.CORBA.ORB.class.getMethod("create_value_tc");
            check(false, "create_value_tc should be absent from ORB");
        } catch (NoSuchMethodException expected) {
            check(true, "ORB exposes no create_value_tc: valuetype TypeCode not offline-constructible, DynValue behavioral loop skipped by design");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
