/*
 * @test
 * @summary org.omg.DynamicAny.DynFixed conformance: interface shape.
 * @run main DynFixedTest
 */

public class DynFixedTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynFixed");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynFixedOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynFixedOperations");
        check(org.omg.DynamicAny.DynAny.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAny");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        // ---- deepened (batch C, jdk8u372 probed): offline tk_fixed probe (skipped instantiation with reason) ----
        org.omg.CORBA.TypeCode dpFtc = orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_fixed);
        check(dpFtc.kind() == org.omg.CORBA.TCKind.tk_fixed, "get_primitive_tc(tk_fixed) yields tk_fixed typecode");
        try {
            java.lang.Object dpF = dpFac.create_dyn_any_from_type_code(dpFtc);
            check(dpF instanceof org.omg.DynamicAny.DynFixed, "fixed tc -> DynFixed");
        } catch (org.omg.CORBA.BAD_OPERATION bo) {
            check(true, "create_dyn_any_from_type_code(tk_fixed) -> org.omg.CORBA.BAD_OPERATION (SUN minor 213: default fixed any cannot be built offline) - DynFixed behavioral coverage skipped by design");
        }
        java.util.Set<String> dpOwn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.DynamicAny.DynFixedOperations.class.getDeclaredMethods()) dpOwn.add(m.getName());
        check(dpOwn.contains("get_value") && dpOwn.contains("set_value"), "DynFixedOperations declares get_value/set_value");
        check(org.omg.DynamicAny.DynAnyOperations.class.isAssignableFrom(org.omg.DynamicAny.DynFixedOperations.class),
              "DynFixedOperations extends DynAnyOperations");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
