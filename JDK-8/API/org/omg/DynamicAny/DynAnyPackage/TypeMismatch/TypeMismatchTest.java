/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyPackage.TypeMismatch conformance: UserException hierarchy and constructors.
 * @run main TypeMismatchTest
 */

public class TypeMismatchTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.DynamicAny.DynAnyPackage.TypeMismatch.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.DynamicAny.DynAnyPackage.TypeMismatch.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.DynamicAny.DynAnyPackage.TypeMismatch.class), "implements IDLEntity");
        org.omg.DynamicAny.DynAnyPackage.TypeMismatch e = new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        org.omg.DynamicAny.DynAnyPackage.TypeMismatch em = new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): idlj UserException pattern ----
        org.omg.DynamicAny.DynAnyPackage.TypeMismatch dpEx0 = new org.omg.DynamicAny.DynAnyPackage.TypeMismatch();
        check(dpEx0.getMessage().equals("IDL:omg.org/DynamicAny/DynAny/TypeMismatch:1.0"), "no-arg getMessage() == repository id");
        check(dpEx0.toString().equals("org.omg.DynamicAny.DynAnyPackage.TypeMismatch: IDL:omg.org/DynamicAny/DynAny/TypeMismatch:1.0"), "no-arg toString exact");
        org.omg.DynamicAny.DynAnyPackage.TypeMismatch dpEx1 = new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("boom");
        check(dpEx1.getMessage().equals("IDL:omg.org/DynamicAny/DynAny/TypeMismatch:1.0  boom"), "reason ctor getMessage() == repoId + two spaces + reason");
        check(dpEx1.toString().equals("org.omg.DynamicAny.DynAnyPackage.TypeMismatch: IDL:omg.org/DynamicAny/DynAny/TypeMismatch:1.0  boom"), "reason ctor toString exact (two spaces before reason)");
        check(org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.id().equals("IDL:omg.org/DynamicAny/DynAny/TypeMismatch:1.0"), "Helper.id() exact");
        org.omg.CORBA.TypeCode dpTc = org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.type();
        check(dpTc.kind() == org.omg.CORBA.TCKind.tk_except, "Helper.type() tk_except");
        check(dpTc.name().equals("TypeMismatch"), "Helper.type().name() TypeMismatch");
        check(dpTc.member_count() == 0, "Helper.type() has no members");
        org.omg.CORBA.Any dpAex = orb.create_any();
        org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.insert(dpAex, dpEx1);
        org.omg.DynamicAny.DynAnyPackage.TypeMismatch dpRt = org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.extract(dpAex);
        check(dpRt.getMessage().equals("IDL:omg.org/DynamicAny/DynAny/TypeMismatch:1.0"), "Any round-trip drops reason text (wire carries repoId only)");
        org.omg.CORBA.portable.OutputStream dpOex = orb.create_output_stream();
        org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.write(dpOex, dpEx1);
        check(org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.read(dpOex.create_input_stream()).getMessage().equals("IDL:omg.org/DynamicAny/DynAny/TypeMismatch:1.0"), "CDR stream round-trip drops reason text");
        org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.insert(dpAex, dpEx0);
        check(org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.extract(dpAex).getMessage().equals("IDL:omg.org/DynamicAny/DynAny/TypeMismatch:1.0"), "no-arg exception Any round-trip");
        // live re-raise through a real operation
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        org.omg.CORBA.Any dpL0 = orb.create_any(); dpL0.insert_boolean(false);
        org.omg.CORBA.Any dpL1 = orb.create_any(); dpL1.insert_boolean(true);
        org.omg.CORBA.UnionMember[] dpUm = new org.omg.CORBA.UnionMember[]{
            new org.omg.CORBA.UnionMember("a", dpL0, orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long), null),
            new org.omg.CORBA.UnionMember("b", dpL1, orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string), null)};
        org.omg.DynamicAny.DynUnion dpUn = (org.omg.DynamicAny.DynUnion) dpFac.create_dyn_any_from_type_code(
            orb.create_union_tc("IDL:U:1.0", "U", orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean), dpUm));
        try {
            dpUn.set_to_default_member();
            throw new AssertionError("set_to_default_member must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.TypeMismatch expected) {
            check(expected.getMessage().equals("IDL:omg.org/DynamicAny/DynAny/TypeMismatch:1.0"),
                  "live re-raise: union without default arm carries repoId message");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
