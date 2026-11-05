/*
 * @test
 * @summary org.omg.DynamicAny.NameDynAnyPairSeqHelper conformance: helper static id/type/narrow.
 * @run main NameDynAnyPairSeqHelperTest
 */

public class NameDynAnyPairSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.NameDynAnyPairSeqHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.NameDynAnyPairSeqHelper.type() != null, "type() non-null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): alias chain + local-objref limit ----
        check(org.omg.DynamicAny.NameDynAnyPairSeqHelper.id().equals("IDL:omg.org/DynamicAny/NameDynAnyPairSeq:1.0"), "id() exact");
        org.omg.CORBA.TypeCode dpTc = org.omg.DynamicAny.NameDynAnyPairSeqHelper.type();
        check(dpTc.kind() == org.omg.CORBA.TCKind.tk_alias, "type() tk_alias");
        check(dpTc.content_type().kind() == org.omg.CORBA.TCKind.tk_sequence, "alias content tk_sequence");
        check(dpTc.content_type().content_type().kind() == org.omg.CORBA.TCKind.tk_struct
              && dpTc.content_type().content_type().name().equals("NameDynAnyPair"),
              "sequence element is the NameDynAnyPair struct");
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        org.omg.DynamicAny.DynAny dpD = dpFac.create_dyn_any_from_type_code(
            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
        org.omg.CORBA.Any dpA = orb.create_any();
        try {
            org.omg.DynamicAny.NameDynAnyPairSeqHelper.insert(dpA,
                new org.omg.DynamicAny.NameDynAnyPair[]{new org.omg.DynamicAny.NameDynAnyPair("m", dpD)});
            throw new AssertionError("insert(local objref seq) expected MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "insert(local DynAny sequence) -> org.omg.CORBA.MARSHAL (objref members cannot marshal offline)");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
