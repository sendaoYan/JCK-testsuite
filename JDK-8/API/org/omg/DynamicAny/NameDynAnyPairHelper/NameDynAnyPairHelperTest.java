/*
 * @test
 * @summary org.omg.DynamicAny.NameDynAnyPairHelper conformance: helper static id/type/narrow.
 * @run main NameDynAnyPairHelperTest
 */

public class NameDynAnyPairHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.NameDynAnyPairHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.NameDynAnyPairHelper.type() != null, "type() non-null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): struct typecode + local-objref marshal limit ----
        check(org.omg.DynamicAny.NameDynAnyPairHelper.id().equals("IDL:omg.org/DynamicAny/NameDynAnyPair:1.0"), "id() exact");
        org.omg.CORBA.TypeCode dpTc = org.omg.DynamicAny.NameDynAnyPairHelper.type();
        check(dpTc.kind() == org.omg.CORBA.TCKind.tk_struct, "type() tk_struct");
        check(dpTc.name().equals("NameDynAnyPair"), "type().name() NameDynAnyPair");
        check(dpTc.member_count() == 2, "member_count 2");
        check(dpTc.member_type(0).kind() == org.omg.CORBA.TCKind.tk_alias
              && dpTc.member_type(0).name().equals("FieldName"), "member id is FieldName alias");
        check(dpTc.member_type(1).kind() == org.omg.CORBA.TCKind.tk_objref, "member value tk_objref (DynAny reference)");
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        org.omg.DynamicAny.DynAny dpD = dpFac.create_dyn_any_from_type_code(
            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
        org.omg.CORBA.Any dpA = orb.create_any();
        try {
            org.omg.DynamicAny.NameDynAnyPairHelper.insert(dpA,
                new org.omg.DynamicAny.NameDynAnyPair("m", dpD));
            throw new AssertionError("insert(local objref) expected MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "insert(local DynAny) -> org.omg.CORBA.MARSHAL (local object ref cannot marshal offline)");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
