/*
 * @test
 * @summary org.omg.CORBA.OctetSeqHelper conformance: helper static id/type/narrow.
 * @run main OctetSeqHelperTest
 */

public class OctetSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.OctetSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.OctetSeqHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.OctetSeqHelper.id().equals("IDL:omg.org/CORBA/OctetSeq:1.0"),
              "OctetSeqHelper.id() exact repository id");
        check(org.omg.CORBA.OctetSeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "OctetSeqHelper.type().kind() == tk_alias");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        org.omg.CORBA.OctetSeqHelper.write(osH, new byte[]{1, 2, 3, 4});
        check(java.util.Arrays.equals(org.omg.CORBA.OctetSeqHelper.read(osH.create_input_stream()), new byte[]{1, 2, 3, 4}),
              "OctetSeqHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyH = orb.create_any();
        org.omg.CORBA.OctetSeqHelper.insert(anyH, new byte[]{1, 2, 3, 4});
        check(java.util.Arrays.equals(org.omg.CORBA.OctetSeqHelper.extract(anyH), new byte[]{1, 2, 3, 4}),
              "OctetSeqHelper.insert/extract Any round-trip");
        check(org.omg.CORBA.OctetSeqHelper.type().content_type().kind() == org.omg.CORBA.TCKind.tk_sequence,
              "OctetSeqHelper.type().content_type().kind() == tk_sequence");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
