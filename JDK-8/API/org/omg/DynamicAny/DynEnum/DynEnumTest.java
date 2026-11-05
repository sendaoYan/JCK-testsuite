/*
 * @test
 * @summary org.omg.DynamicAny.DynEnum conformance: interface shape.
 * @run main DynEnumTest
 */

public class DynEnumTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynEnum");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynEnumOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynEnumOperations");
        check(org.omg.DynamicAny.DynAny.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAny");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        // ---- deepened (batch C, jdk8u372 probed): enum state machine over the wire type ----
        org.omg.CORBA.TypeCode dpEtc = orb.create_enum_tc("IDL:T:1.0", "T", new String[]{"A", "B", "C"});
        org.omg.DynamicAny.DynEnum dpEn = (org.omg.DynamicAny.DynEnum) dpFac.create_dyn_any_from_type_code(dpEtc);
        check(dpEn != null, "enum tc yields DynEnum instance");
        check(dpEn.get_as_string().equals("A"), "default member is the FIRST enum member A");
        check(dpEn.get_as_ulong() == 0, "default as_ulong == 0");
        dpEn.set_as_string("B");
        check(dpEn.get_as_string().equals("B") && dpEn.get_as_ulong() == 1, "set_as_string(B) updates both views");
        dpEn.set_as_ulong(2);
        check(dpEn.get_as_string().equals("C") && dpEn.get_as_ulong() == 2, "set_as_ulong(2) updates both views");
        try {
            dpEn.set_as_string("NOPE");
            throw new AssertionError("set_as_string(NOPE) must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.InvalidValue expected) {
            check(true, "set_as_string(unknown name) -> DynAnyPackage.InvalidValue");
        }
        try {
            dpEn.set_as_ulong(9);
            throw new AssertionError("set_as_ulong(9) must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.InvalidValue expected) {
            check(true, "set_as_ulong(out of range) -> DynAnyPackage.InvalidValue");
        }
        check(dpEn.component_count() == 0, "enum component_count()==0 (leaf value)");
        check(dpEn.type().kind() == org.omg.CORBA.TCKind.tk_enum, "type() stays tk_enum");
        org.omg.CORBA.Any dpEa = dpEn.to_any();
        check(dpEa.type().kind() == org.omg.CORBA.TCKind.tk_enum, "to_any() carries tk_enum");
        check(dpEn.equal(dpEn.copy()), "equal(copy) true for enum");
        org.omg.DynamicAny.DynEnum dpEn2 = (org.omg.DynamicAny.DynEnum) dpFac.create_dyn_any_from_type_code(dpEtc);
        dpEn2.set_as_ulong(0);
        check(!dpEn.equal(dpEn2), "equal false for different enum members");
        // set_as_string("") boundary
        try {
            dpEn2.set_as_string("");
            throw new AssertionError("set_as_string(empty) must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.InvalidValue expected) {
            check(true, "set_as_string(empty) -> InvalidValue");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
