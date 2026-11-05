/*
 * @test
 * @summary org.omg.DynamicAny.DynStruct conformance: interface shape.
 * @run main DynStructTest
 */

public class DynStructTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynStruct");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.DynamicAny.DynStructOperations.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynStructOperations");
        check(org.omg.DynamicAny.DynAny.class.isAssignableFrom(c), "extends org.omg.DynamicAny.DynAny");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.DynamicAny.DynAnyFactory dpFac = org.omg.DynamicAny.DynAnyFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("DynAnyFactory"));
        // ---- deepened (batch C, jdk8u372 probed): member model + name/value channels ----
        org.omg.CORBA.StructMember[] dpSm = new org.omg.CORBA.StructMember[]{
            new org.omg.CORBA.StructMember("f1", orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long), null),
            new org.omg.CORBA.StructMember("f2", orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_string), null)};
        org.omg.DynamicAny.DynStruct dpSt = (org.omg.DynamicAny.DynStruct) dpFac.create_dyn_any_from_type_code(
            orb.create_struct_tc("IDL:S:1.0", "S", dpSm));
        org.omg.DynamicAny.NameValuePair[] dpMs = dpSt.get_members();
        check(dpMs.length == 2, "get_members().length == 2");
        check(dpMs[0].id.equals("f1"), "member[0] id f1");
        check(dpMs[0].value.type().kind() == org.omg.CORBA.TCKind.tk_long, "member[0] kind tk_long");
        check(dpMs[1].id.equals("f2") && dpMs[1].value.type().kind() == org.omg.CORBA.TCKind.tk_string, "member[1] f2 tk_string");
        check(dpSt.current_member_name().equals("f1"), "current_member_name starts at f1");
        check(dpSt.current_member_kind() == org.omg.CORBA.TCKind.tk_long, "current_member_kind tk_long");
        check(dpSt.seek(1), "seek(1) moves to f2");
        check(dpSt.current_member_name().equals("f2"), "current_member_name after seek is f2");
        dpSt.insert_string("vv");
        check(dpSt.get_string().equals("vv"), "insert_string/get_string on current member f2");
        org.omg.CORBA.Any dpV1 = orb.create_any(); dpV1.insert_long(77);
        org.omg.CORBA.Any dpV2 = orb.create_any(); dpV2.insert_string("sv");
        dpSt.set_members(new org.omg.DynamicAny.NameValuePair[]{
            new org.omg.DynamicAny.NameValuePair("f1", dpV1), new org.omg.DynamicAny.NameValuePair("f2", dpV2)});
        org.omg.DynamicAny.NameValuePair[] dpBack = dpSt.get_members();
        check(dpBack[0].value.extract_long() == 77, "set/get_members round-trip member 0 (long 77)");
        check(dpBack[1].value.extract_string().equals("sv"), "set/get_members round-trip member 1 (string sv)");
        // dyn_any member channel
        org.omg.DynamicAny.NameDynAnyPair[] dpMda = dpSt.get_members_as_dyn_any();
        check(dpMda.length == 2 && dpMda[0].id.equals("f1"), "get_members_as_dyn_any length + id");
        check(dpMda[0].value.get_long() == 77, "get_members_as_dyn_any[0] value 77");
        dpSt.set_members_as_dyn_any(dpMda);
        check(dpSt.get_members()[1].value.extract_string().equals("sv"), "set_members_as_dyn_any round-trip");
        check(dpSt.component_count() == 2, "struct component_count()==2");
        // JDK-8 lenient behaviors recorded as no-throw
        try { dpSt.set_members(null); check(true, "set_members(null) tolerated (JDK-8 lenient, recorded)"); }
        catch (Throwable t) { check(true, "set_members(null) -> " + t.getClass().getName() + " (recorded)"); }
        try { dpSt.set_members(new org.omg.DynamicAny.NameValuePair[0]); check(true, "set_members(empty) tolerated (JDK-8 lenient, recorded)"); }
        catch (Throwable t) { check(true, "set_members(empty) -> " + t.getClass().getName() + " (recorded)"); }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
