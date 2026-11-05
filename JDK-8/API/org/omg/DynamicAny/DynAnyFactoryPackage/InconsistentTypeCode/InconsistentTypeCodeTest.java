/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode conformance: UserException hierarchy and constructors.
 * @run main InconsistentTypeCodeTest
 */

public class InconsistentTypeCodeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode.class), "implements IDLEntity");
        org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode e = new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode em = new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): idlj UserException pattern ----
        org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode dpEx0 = new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode();
        check(dpEx0.getMessage().equals("IDL:omg.org/DynamicAny/DynAnyFactory/InconsistentTypeCode:1.0"), "no-arg getMessage() == repository id");
        check(dpEx0.toString().equals("org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode: IDL:omg.org/DynamicAny/DynAnyFactory/InconsistentTypeCode:1.0"), "no-arg toString exact");
        org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode dpEx1 = new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode("boom");
        check(dpEx1.getMessage().equals("IDL:omg.org/DynamicAny/DynAnyFactory/InconsistentTypeCode:1.0  boom"), "reason ctor getMessage() == repoId + two spaces + reason");
        check(dpEx1.toString().equals("org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode: IDL:omg.org/DynamicAny/DynAnyFactory/InconsistentTypeCode:1.0  boom"), "reason ctor toString exact (two spaces before reason)");
        check(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.id().equals("IDL:omg.org/DynamicAny/DynAnyFactory/InconsistentTypeCode:1.0"), "Helper.id() exact");
        org.omg.CORBA.TypeCode dpTc = org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.type();
        check(dpTc.kind() == org.omg.CORBA.TCKind.tk_except, "Helper.type() tk_except");
        check(dpTc.name().equals("InconsistentTypeCode"), "Helper.type().name() InconsistentTypeCode");
        check(dpTc.member_count() == 0, "Helper.type() has no members");
        org.omg.CORBA.Any dpAex = orb.create_any();
        org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.insert(dpAex, dpEx1);
        org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode dpRt = org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.extract(dpAex);
        check(dpRt.getMessage().equals("IDL:omg.org/DynamicAny/DynAnyFactory/InconsistentTypeCode:1.0"), "Any round-trip drops reason text (wire carries repoId only)");
        org.omg.CORBA.portable.OutputStream dpOex = orb.create_output_stream();
        org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.write(dpOex, dpEx1);
        check(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.read(dpOex.create_input_stream()).getMessage().equals("IDL:omg.org/DynamicAny/DynAnyFactory/InconsistentTypeCode:1.0"), "CDR stream round-trip drops reason text");
        org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.insert(dpAex, dpEx0);
        check(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.extract(dpAex).getMessage().equals("IDL:omg.org/DynamicAny/DynAnyFactory/InconsistentTypeCode:1.0"), "no-arg exception Any round-trip");
        // live raise through the factory itself
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        try {
            dpFac.create_dyn_any_from_type_code(orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_except));
            throw new AssertionError("primitive tk_except must throw");
        } catch (org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode expected) {
            check(true, "live: create_dyn_any_from_type_code(primitive tk_except) -> InconsistentTypeCode (no exception members to iterate)");
        }
        // contrast: well-formed exception tc is accepted (recorded; yields a leaf DynAny, count 0)
        org.omg.CORBA.TypeCode dpWell = orb.create_exception_tc("IDL:X/E:1.0", "E", new org.omg.CORBA.StructMember[0]);
        org.omg.DynamicAny.DynAny dpLeaf = dpFac.create_dyn_any_from_type_code(dpWell);
        check(dpLeaf.component_count() == 0, "well-formed exception tc accepted as leaf DynAny (component_count 0)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
