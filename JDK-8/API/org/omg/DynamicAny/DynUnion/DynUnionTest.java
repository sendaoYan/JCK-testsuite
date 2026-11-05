/*
 * @test
 * @summary org.omg.DynamicAny.DynUnion conformance: interface shape.
 * @run main DynUnionTest
 */

public class DynUnionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynUnion");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynUnionOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynUnionOperations");
        check(org.omg.DynamicAny.DynAny.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAny");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        // ---- deepened (batch C, jdk8u372 probed): discriminator-driven member selection ----
        org.omg.CORBA.UnionMember[] dpUm = new org.omg.CORBA.UnionMember[]{
            new org.omg.CORBA.UnionMember("a", orb.create_any(), orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long), null),
            new org.omg.CORBA.UnionMember("b", orb.create_any(), orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string), null)};
        dpUm[0].label.insert_boolean(false);
        dpUm[1].label.insert_boolean(true);
        org.omg.DynamicAny.DynUnion dpUn = (org.omg.DynamicAny.DynUnion) dpFac.create_dyn_any_from_type_code(
            orb.create_union_tc("IDL:U:1.0", "U", orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean), dpUm));
        check(!dpUn.has_no_active_member(), "boolean(false) default selects arm a -> active member exists");
        check(dpUn.member_name().equals("a"), "default member_name a");
        check(dpUn.member().type().kind() == org.omg.CORBA.TCKind.tk_long, "default member kind tk_long");
        check(dpUn.discriminator_kind() == org.omg.CORBA.TCKind.tk_boolean, "discriminator_kind tk_boolean");
        check(dpUn.member_kind() == org.omg.CORBA.TCKind.tk_long, "member_kind tk_long");
        check(dpUn.get_discriminator().type().kind() == org.omg.CORBA.TCKind.tk_boolean, "get_discriminator() type tk_boolean");
        check(dpUn.get_discriminator().get_boolean() == false, "discriminator default value false");
        org.omg.DynamicAny.DynAny dpTrue = dpFac.create_dyn_any_from_type_code(
            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
        dpTrue.insert_boolean(true);
        dpUn.set_discriminator(dpTrue);
        check(dpUn.member_name().equals("b"), "after set_discriminator(true) member_name b");
        check(dpUn.member().type().kind() == org.omg.CORBA.TCKind.tk_string, "arm b member kind tk_string");
        org.omg.DynamicAny.DynAny dpFalse = dpFac.create_dyn_any_from_type_code(
            orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
        dpFalse.insert_boolean(false);
        dpUn.set_discriminator(dpFalse);
        check(dpUn.member_name().equals("a") && dpUn.member().type().kind() == org.omg.CORBA.TCKind.tk_long,
              "set_discriminator(false) switches back to arm a");
        try {
            org.omg.DynamicAny.DynAny dpLongDisc = dpFac.create_dyn_any_from_type_code(
                orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
            dpUn.set_discriminator(dpLongDisc);
            throw new AssertionError("wrong discriminator type must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch expected) {
            check(true, "set_discriminator(tk_long) -> DynAnyPackage.TypeMismatch");
        }
        try {
            dpUn.set_to_default_member();
            throw new AssertionError("union without default arm must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch expected) {
            check(true, "set_to_default_member() on union WITHOUT default arm -> TypeMismatch");
        }
        dpUn.set_to_no_active_member();
        check(dpUn.has_no_active_member(), "set_to_no_active_member() -> has_no_active_member true (JDK-8 allows it)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
