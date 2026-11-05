/*
 * @test
 * @summary org.omg.CORBA.PolicyErrorCodeHelper conformance: helper static id/type/narrow.
 * @run main PolicyErrorCodeHelperTest
 */

public class PolicyErrorCodeHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.PolicyErrorCodeHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.PolicyErrorCodeHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.PolicyErrorCodeHelper.id().equals("IDL:omg.org/CORBA/PolicyErrorCode:1.0"),
              "PolicyErrorCodeHelper.id() exact repository id");
        check(org.omg.CORBA.PolicyErrorCodeHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "PolicyErrorCodeHelper.type().kind() == tk_alias (typedef)");
        check(org.omg.CORBA.PolicyErrorCodeHelper.type().content_type().kind() == org.omg.CORBA.TCKind.tk_short,
              "PolicyErrorCodeHelper.type().content_type().kind() == tk_short");
        org.omg.CORBA.portable.OutputStream osA = orb.create_output_stream();
        org.omg.CORBA.PolicyErrorCodeHelper.write(osA, (short) 4);
        check(org.omg.CORBA.PolicyErrorCodeHelper.read(osA.create_input_stream()) == (short) 4,
              "PolicyErrorCodeHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyA = orb.create_any();
        org.omg.CORBA.PolicyErrorCodeHelper.insert(anyA, (short) 4);
        check(org.omg.CORBA.PolicyErrorCodeHelper.extract(anyA) == (short) 4,
              "PolicyErrorCodeHelper.insert/extract Any round-trip");
        check(anyA.extract_short() == (short) 4,
              "PolicyErrorCodeHelper channel equals Any direct channel (extract_short)");
        check(anyA.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "any keeps the alias TypeCode after PolicyErrorCodeHelper.insert");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
