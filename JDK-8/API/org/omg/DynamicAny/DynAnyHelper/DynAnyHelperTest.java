/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyHelper conformance: helper static id/type/narrow.
 * @run main DynAnyHelperTest
 */

public class DynAnyHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynAnyHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynAnyHelper.type() != null, "type() non-null");
        check(org.omg.DynamicAny.DynAnyHelper.narrow(null) == null, "narrow(null) returns null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        check(dpFac != null, "resolve_initial_references(DynAnyFactory) + narrow works offline");
        // ---- deepened (batch C, jdk8u372 probed): objref helper + local-marshal negative ----
        check(org.omg.DynamicAny.DynAnyHelper.id().equals("IDL:omg.org/DynamicAny/DynAny:1.0"), "id() exact");
        org.omg.CORBA.TypeCode dpTc = org.omg.DynamicAny.DynAnyHelper.type();
        check(dpTc.kind() == org.omg.CORBA.TCKind.tk_objref, "type() tk_objref");
        check(dpTc.name().equals("DynAny"), "type().name() DynAny");
        check(org.omg.DynamicAny.DynAnyHelper.narrow(null) == null, "narrow(null) -> null");
        org.omg.CORBA.Any dpNil = orb.create_any();
        try {
            org.omg.DynamicAny.DynAnyHelper.insert(dpNil, null);
            throw new AssertionError("insert(null) expected MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "insert(nil DynAny) -> org.omg.CORBA.MARSHAL (nil rejected, unlike CosNaming objref helpers)");
        }
        org.omg.CORBA.portable.OutputStream dpOsNil = orb.create_output_stream();
        try {
            org.omg.DynamicAny.DynAnyHelper.write(dpOsNil, null);
            throw new AssertionError("write(null) expected MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "write(nil DynAny) -> org.omg.CORBA.MARSHAL");
        }
        // a LOCAL (never-connected) DynAny cannot be marshaled through the Helper
        org.omg.DynamicAny.DynAny dpLocal = dpFac.create_dyn_any_from_type_code(
            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
        org.omg.CORBA.Any dpBad = orb.create_any();
        try {
            org.omg.DynamicAny.DynAnyHelper.insert(dpBad, dpLocal);
            throw new AssertionError("insert of local DynAny must throw MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "insert(local non-stub DynAny) -> org.omg.CORBA.MARSHAL (no object reference to marshal)");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
