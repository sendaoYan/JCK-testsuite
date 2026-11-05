/*
 * @test
 * @summary org.omg.CORBA.CharSeqHelper conformance: helper static id/type/narrow.
 * @run main CharSeqHelperTest
 */

public class CharSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.CharSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.CharSeqHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.CharSeqHelper.id().equals("IDL:omg.org/CORBA/CharSeq:1.0"),
              "CharSeqHelper.id() exact repository id");
        check(org.omg.CORBA.CharSeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "CharSeqHelper.type().kind() == tk_alias");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        org.omg.CORBA.CharSeqHelper.write(osH, new char[]{'a'});
        check(java.util.Arrays.equals(org.omg.CORBA.CharSeqHelper.read(osH.create_input_stream()), new char[]{'a'}),
              "CharSeqHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyH = orb.create_any();
        org.omg.CORBA.CharSeqHelper.insert(anyH, new char[]{'a'});
        check(java.util.Arrays.equals(org.omg.CORBA.CharSeqHelper.extract(anyH), new char[]{'a'}),
              "CharSeqHelper.insert/extract Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
