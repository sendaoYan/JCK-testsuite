/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyFactory conformance: interface shape.
 * @run main DynAnyFactoryTest
 */

public class DynAnyFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynAnyFactory");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynAnyFactoryOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAnyFactoryOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        check(dpFac != null, "resolve_initial_references(DynAnyFactory) + narrow works offline");
        // ---- deepened (batch C, jdk8u372 probed): offline create_dyn_any matrix + type-code dispatch ----
        org.omg.CORBA.Any dpL = orb.create_any(); dpL.insert_long(42);
        org.omg.DynamicAny.DynAny dpDl = dpFac.create_dyn_any(dpL);
        check(dpDl.type().kind() == org.omg.CORBA.TCKind.tk_long, "create_dyn_any(long any): type tk_long");
        check(dpDl.get_long() == 42, "create_dyn_any(long 42) -> get_long()==42");
        org.omg.CORBA.Any dpS = orb.create_any(); dpS.insert_string("str0");
        check(dpFac.create_dyn_any(dpS).get_string().equals("str0"), "create_dyn_any(string any) -> get_string");
        org.omg.CORBA.Any dpD2 = orb.create_any(); dpD2.insert_double(2.5);
        check(dpFac.create_dyn_any(dpD2).get_double() == 2.5, "create_dyn_any(double 2.5) -> get_double");
        org.omg.CORBA.Any dpB = orb.create_any(); dpB.insert_boolean(true);
        check(dpFac.create_dyn_any(dpB).get_boolean(), "create_dyn_any(boolean true) -> get_boolean");
        org.omg.CORBA.Any dpO = orb.create_any(); dpO.insert_octet((byte) 7);
        check(dpFac.create_dyn_any(dpO).get_octet() == 7, "create_dyn_any(octet 7) -> get_octet");
        check(dpFac.create_dyn_any(orb.create_any()).type().kind() == org.omg.CORBA.TCKind.tk_null,
              "create_dyn_any(empty any) -> type tk_null");
        try {
            dpFac.create_dyn_any(null);
            throw new AssertionError("create_dyn_any(null) must throw");
        } catch (org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode expected) {
            check(true, "create_dyn_any(null) -> org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode");
        }
        try {
            dpFac.create_dyn_any_from_type_code(null);
            throw new AssertionError("create_dyn_any_from_type_code(null) must throw");
        } catch (org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode expected) {
            check(true, "create_dyn_any_from_type_code(null) -> InconsistentTypeCode");
        }
        // most-derived dispatch from type codes
        check(dpFac.create_dyn_any_from_type_code(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long))
              instanceof org.omg.DynamicAny.DynAny, "tk_long tc -> plain DynAny");
        java.lang.Object dpEnum = dpFac.create_dyn_any_from_type_code(
            orb.create_enum_tc("IDL:T:1.0", "T", new String[]{"A", "B"}));
        check(dpEnum instanceof org.omg.DynamicAny.DynEnum, "enum tc -> DynEnum");
        check(!(dpEnum instanceof org.omg.DynamicAny.DynStruct), "enum tc is NOT DynStruct");
        java.lang.Object dpStruct = dpFac.create_dyn_any_from_type_code(
            orb.create_struct_tc("IDL:S:1.0", "S", new org.omg.CORBA.StructMember[]{
                new org.omg.CORBA.StructMember("f1", orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long), null)}));
        check(dpStruct instanceof org.omg.DynamicAny.DynStruct, "struct tc -> DynStruct");
        java.lang.Object dpSeq = dpFac.create_dyn_any_from_type_code(
            orb.create_sequence_tc(0, orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long)));
        check(dpSeq instanceof org.omg.DynamicAny.DynSequence, "sequence tc -> DynSequence");
        java.lang.Object dpArr = dpFac.create_dyn_any_from_type_code(
            orb.create_array_tc(2, orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long)));
        check(dpArr instanceof org.omg.DynamicAny.DynArray, "array tc -> DynArray");
        java.lang.Object dpUni = dpFac.create_dyn_any_from_type_code(
            orb.create_union_tc("IDL:U:1.0", "U", orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean),
                new org.omg.CORBA.UnionMember[]{
                    new org.omg.CORBA.UnionMember("a", orb.create_any(), orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long), null)}));
        check(dpUni instanceof org.omg.DynamicAny.DynUnion, "union tc -> DynUnion");
        // enum value survives the Any channel into a DynEnum
        org.omg.CORBA.Any dpEnumAny = ((org.omg.DynamicAny.DynEnum) dpEnum).to_any();
        check(dpEnumAny.type().kind() == org.omg.CORBA.TCKind.tk_enum, "enum DynAny.to_any kind tk_enum");
        org.omg.DynamicAny.DynEnum dpEnumViaAny = (org.omg.DynamicAny.DynEnum) dpFac.create_dyn_any(dpEnumAny);
        check(dpEnumViaAny.get_as_string().equals("A"), "enum Any channel round-trip preserves default member A");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
