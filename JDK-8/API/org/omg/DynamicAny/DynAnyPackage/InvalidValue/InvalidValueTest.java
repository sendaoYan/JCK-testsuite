/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyPackage.InvalidValue conformance: UserException hierarchy and constructors.
 * @run main InvalidValueTest
 */

public class InvalidValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.DynamicAny.DynAnyPackage.InvalidValue.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.DynamicAny.DynAnyPackage.InvalidValue.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.DynamicAny.DynAnyPackage.InvalidValue.class), "implements IDLEntity");
        org.omg.DynamicAny.DynAnyPackage.InvalidValue e = new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        org.omg.DynamicAny.DynAnyPackage.InvalidValue em = new org.omg.DynamicAny.DynAnyPackage.InvalidValue("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): idlj UserException pattern ----
        org.omg.DynamicAny.DynAnyPackage.InvalidValue dpEx0 = new org.omg.DynamicAny.DynAnyPackage.InvalidValue();
        check(dpEx0.getMessage().equals("IDL:omg.org/DynamicAny/DynAny/InvalidValue:1.0"), "no-arg getMessage() == repository id");
        check(dpEx0.toString().equals("org.omg.DynamicAny.DynAnyPackage.InvalidValue: IDL:omg.org/DynamicAny/DynAny/InvalidValue:1.0"), "no-arg toString exact");
        org.omg.DynamicAny.DynAnyPackage.InvalidValue dpEx1 = new org.omg.DynamicAny.DynAnyPackage.InvalidValue("boom");
        check(dpEx1.getMessage().equals("IDL:omg.org/DynamicAny/DynAny/InvalidValue:1.0  boom"), "reason ctor getMessage() == repoId + two spaces + reason");
        check(dpEx1.toString().equals("org.omg.DynamicAny.DynAnyPackage.InvalidValue: IDL:omg.org/DynamicAny/DynAny/InvalidValue:1.0  boom"), "reason ctor toString exact (two spaces before reason)");
        check(org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.id().equals("IDL:omg.org/DynamicAny/DynAny/InvalidValue:1.0"), "Helper.id() exact");
        org.omg.CORBA.TypeCode dpTc = org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.type();
        check(dpTc.kind() == org.omg.CORBA.TCKind.tk_except, "Helper.type() tk_except");
        check(dpTc.name().equals("InvalidValue"), "Helper.type().name() InvalidValue");
        check(dpTc.member_count() == 0, "Helper.type() has no members");
        org.omg.CORBA.Any dpAex = orb.create_any();
        org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.insert(dpAex, dpEx1);
        org.omg.DynamicAny.DynAnyPackage.InvalidValue dpRt = org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.extract(dpAex);
        check(dpRt.getMessage().equals("IDL:omg.org/DynamicAny/DynAny/InvalidValue:1.0"), "Any round-trip drops reason text (wire carries repoId only)");
        org.omg.CORBA.portable.OutputStream dpOex = orb.create_output_stream();
        org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.write(dpOex, dpEx1);
        check(org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.read(dpOex.create_input_stream()).getMessage().equals("IDL:omg.org/DynamicAny/DynAny/InvalidValue:1.0"), "CDR stream round-trip drops reason text");
        org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.insert(dpAex, dpEx0);
        check(org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.extract(dpAex).getMessage().equals("IDL:omg.org/DynamicAny/DynAny/InvalidValue:1.0"), "no-arg exception Any round-trip");
        // live re-raise through a real operation
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        org.omg.CORBA.TypeCode dpEtc = orb.create_enum_tc("IDL:T:1.0", "T", new String[]{"A", "B"});
        org.omg.DynamicAny.DynEnum dpEn = (org.omg.DynamicAny.DynEnum) dpFac.create_dyn_any_from_type_code(dpEtc);
        try {
            dpEn.set_as_string("NOPE");
            throw new AssertionError("set_as_string(NOPE) must throw");
        } catch (org.omg.DynamicAny.DynAnyPackage.InvalidValue expected) {
            check(expected.getMessage().equals("IDL:omg.org/DynamicAny/DynAny/InvalidValue:1.0"),
                  "live re-raise: DynEnum.set_as_string(unknown) carries repoId message");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
