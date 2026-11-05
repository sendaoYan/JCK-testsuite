/*
 * @test
 * @summary org.omg.DynamicAny.DynAnySeqHelper conformance: helper static id/type/narrow.
 * @run main DynAnySeqHelperTest
 */

public class DynAnySeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynAnySeqHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynAnySeqHelper.type() != null, "type() non-null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        check(dpFac != null, "resolve_initial_references(DynAnyFactory) + narrow works offline");
        // ---- deepened (batch C, jdk8u372 probed): sequence-of-objref helper + local-marshal negative ----
        check(org.omg.DynamicAny.DynAnySeqHelper.id().equals("IDL:omg.org/DynamicAny/DynAnySeq:1.0"), "id() exact");
        check(org.omg.DynamicAny.DynAnySeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias, "type() tk_alias (typedef)");
        check(org.omg.DynamicAny.DynAnySeqHelper.type().content_type().kind() == org.omg.CORBA.TCKind.tk_sequence,
              "content tk_sequence");
        org.omg.DynamicAny.DynAny dpLocal = dpFac.create_dyn_any_from_type_code(
            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
        org.omg.CORBA.Any dpBad = orb.create_any();
        try {
            org.omg.DynamicAny.DynAnySeqHelper.insert(dpBad, new org.omg.DynamicAny.DynAny[]{dpLocal});
            throw new AssertionError("insert of local DynAny array must throw MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "DynAnySeqHelper.insert(local[]) -> MARSHAL");
        }
        // empty array still needs to marshal zero object refs: succeeds
        org.omg.CORBA.Any dpEmpty = orb.create_any();
        org.omg.DynamicAny.DynAnySeqHelper.insert(dpEmpty, new org.omg.DynamicAny.DynAny[0]);
        check(org.omg.DynamicAny.DynAnySeqHelper.extract(dpEmpty).length == 0, "empty array Any round-trip length 0");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
