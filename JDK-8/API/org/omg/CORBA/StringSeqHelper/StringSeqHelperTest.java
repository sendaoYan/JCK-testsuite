/*
 * @test
 * @summary org.omg.CORBA.StringSeqHelper conformance: helper static id/type/narrow.
 * @run main StringSeqHelperTest
 */

public class StringSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.StringSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.StringSeqHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.StringSeqHelper.id().equals("IDL:omg.org/CORBA/StringSeq:1.0"),
              "StringSeqHelper.id() exact repository id");
        check(org.omg.CORBA.StringSeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "StringSeqHelper.type().kind() == tk_alias");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        org.omg.CORBA.StringSeqHelper.write(osH, new String[]{"a", "", "c"});
        check(java.util.Arrays.equals(org.omg.CORBA.StringSeqHelper.read(osH.create_input_stream()), new String[]{"a", "", "c"}),
              "StringSeqHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyH = orb.create_any();
        org.omg.CORBA.StringSeqHelper.insert(anyH, new String[]{"a", "", "c"});
        check(java.util.Arrays.equals(org.omg.CORBA.StringSeqHelper.extract(anyH), new String[]{"a", "", "c"}),
              "StringSeqHelper.insert/extract Any round-trip");
        check(org.omg.CORBA.StringSeqHelper.type().content_type().kind() == org.omg.CORBA.TCKind.tk_sequence,
              "StringSeqHelper.type().content_type().kind() == tk_sequence");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
