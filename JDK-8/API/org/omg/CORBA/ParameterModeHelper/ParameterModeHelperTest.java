/*
 * @test
 * @summary org.omg.CORBA.ParameterModeHelper conformance: helper static id/type/narrow.
 * @run main ParameterModeHelperTest
 */

public class ParameterModeHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ParameterModeHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ParameterModeHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.ParameterModeHelper.id().equals("IDL:omg.org/CORBA/ParameterMode:1.0"),
              "ParameterModeHelper.id() exact repository id");
        check(org.omg.CORBA.ParameterModeHelper.type().kind() == org.omg.CORBA.TCKind.tk_enum,
              "ParameterModeHelper.type().kind() == tk_enum");
        org.omg.CORBA.portable.OutputStream osE = orb.create_output_stream();
        org.omg.CORBA.ParameterModeHelper.write(osE, org.omg.CORBA.ParameterMode.PARAM_INOUT);
        check(org.omg.CORBA.ParameterModeHelper.read(osE.create_input_stream()) == org.omg.CORBA.ParameterMode.PARAM_INOUT,
              "ParameterModeHelper.write/read round-trips to the canonical constant (identity)");
        org.omg.CORBA.Any anyE = orb.create_any();
        org.omg.CORBA.ParameterModeHelper.insert(anyE, org.omg.CORBA.ParameterMode.PARAM_OUT);
        check(org.omg.CORBA.ParameterModeHelper.extract(anyE) == org.omg.CORBA.ParameterMode.PARAM_OUT,
              "ParameterModeHelper.insert/extract round-trips to the canonical constant (identity)");
        check(org.omg.CORBA.ParameterMode._PARAM_IN == 0 && org.omg.CORBA.ParameterMode._PARAM_OUT == 1
              && org.omg.CORBA.ParameterMode._PARAM_INOUT == 2, "ParameterMode constants are {0,1,2}");
        check(org.omg.CORBA.ParameterMode.from_int(1) == org.omg.CORBA.ParameterMode.PARAM_OUT,
              "ParameterMode.from_int(1) normalizes to PARAM_OUT");
        try {
            org.omg.CORBA.ParameterMode.from_int(9);
            throw new AssertionError("ParameterMode.from_int(9) should throw BAD_PARAM");
        } catch (org.omg.CORBA.BAD_PARAM expected) {
            check(true, "ParameterMode.from_int(9) -> BAD_PARAM");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
