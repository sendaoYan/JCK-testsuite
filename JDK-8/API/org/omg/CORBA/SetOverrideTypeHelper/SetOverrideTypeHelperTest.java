/*
 * @test
 * @summary org.omg.CORBA.SetOverrideTypeHelper conformance: helper static id/type/narrow.
 * @run main SetOverrideTypeHelperTest
 */

public class SetOverrideTypeHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.SetOverrideTypeHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.SetOverrideTypeHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.SetOverrideTypeHelper.id().equals("IDL:omg.org/CORBA/SetOverrideType:1.0"),
              "SetOverrideTypeHelper.id() exact repository id");
        check(org.omg.CORBA.SetOverrideTypeHelper.type().kind() == org.omg.CORBA.TCKind.tk_enum,
              "SetOverrideTypeHelper.type().kind() == tk_enum");
        org.omg.CORBA.portable.OutputStream osE = orb.create_output_stream();
        org.omg.CORBA.SetOverrideTypeHelper.write(osE, org.omg.CORBA.SetOverrideType.SET_OVERRIDE);
        check(org.omg.CORBA.SetOverrideTypeHelper.read(osE.create_input_stream()) == org.omg.CORBA.SetOverrideType.SET_OVERRIDE,
              "SetOverrideTypeHelper.write/read round-trips to the canonical constant (identity)");
        org.omg.CORBA.Any anyE = orb.create_any();
        org.omg.CORBA.SetOverrideTypeHelper.insert(anyE, org.omg.CORBA.SetOverrideType.ADD_OVERRIDE);
        check(org.omg.CORBA.SetOverrideTypeHelper.extract(anyE) == org.omg.CORBA.SetOverrideType.ADD_OVERRIDE,
              "SetOverrideTypeHelper.insert/extract round-trips to the canonical constant (identity)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
