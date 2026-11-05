/*
 * @test
 * @summary org.omg.CORBA.WCharSeqHelper conformance: helper static id/type/narrow.
 * @run main WCharSeqHelperTest
 */

public class WCharSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.WCharSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.WCharSeqHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.WCharSeqHelper.id().equals("IDL:omg.org/CORBA/WCharSeq:1.0"),
              "WCharSeqHelper.id() exact repository id");
        check(org.omg.CORBA.WCharSeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "WCharSeqHelper.type().kind() == tk_alias");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        org.omg.CORBA.WCharSeqHelper.write(osH, new char[]{'\u4f60', 'a'});
        check(java.util.Arrays.equals(org.omg.CORBA.WCharSeqHelper.read(osH.create_input_stream()), new char[]{'\u4f60', 'a'}),
              "WCharSeqHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyH = orb.create_any();
        org.omg.CORBA.WCharSeqHelper.insert(anyH, new char[]{'\u4f60', 'a'});
        check(java.util.Arrays.equals(org.omg.CORBA.WCharSeqHelper.extract(anyH), new char[]{'\u4f60', 'a'}),
              "WCharSeqHelper.insert/extract Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
