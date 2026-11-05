/*
 * @test
 * @summary org.omg.CORBA.PolicyTypeHelper conformance: helper static id/type/narrow.
 * @run main PolicyTypeHelperTest
 */

public class PolicyTypeHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.PolicyTypeHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.PolicyTypeHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.PolicyTypeHelper.id().equals("IDL:omg.org/CORBA/PolicyType:1.0"),
              "PolicyTypeHelper.id() exact repository id");
        check(org.omg.CORBA.PolicyTypeHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "PolicyTypeHelper.type().kind() == tk_alias (typedef)");
        check(org.omg.CORBA.PolicyTypeHelper.type().content_type().kind() == org.omg.CORBA.TCKind.tk_ulong,
              "PolicyTypeHelper.type().content_type().kind() == tk_ulong");
        org.omg.CORBA.portable.OutputStream osA = orb.create_output_stream();
        org.omg.CORBA.PolicyTypeHelper.write(osA, 77);
        check(org.omg.CORBA.PolicyTypeHelper.read(osA.create_input_stream()) == 77,
              "PolicyTypeHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyA = orb.create_any();
        org.omg.CORBA.PolicyTypeHelper.insert(anyA, 77);
        check(org.omg.CORBA.PolicyTypeHelper.extract(anyA) == 77,
              "PolicyTypeHelper.insert/extract Any round-trip");
        check(anyA.extract_ulong() == 77,
              "PolicyTypeHelper channel equals Any direct channel (extract_ulong)");
        check(anyA.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "any keeps the alias TypeCode after PolicyTypeHelper.insert");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
