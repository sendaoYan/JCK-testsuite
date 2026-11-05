/*
 * @test
 * @summary org.omg.CORBA.UShortSeqHelper conformance: helper static id/type/narrow.
 * @run main UShortSeqHelperTest
 */

public class UShortSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UShortSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.UShortSeqHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.UShortSeqHelper.id().equals("IDL:omg.org/CORBA/UShortSeq:1.0"),
              "UShortSeqHelper.id() exact repository id");
        check(org.omg.CORBA.UShortSeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "UShortSeqHelper.type().kind() == tk_alias");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        org.omg.CORBA.UShortSeqHelper.write(osH, new short[]{(short) 4000});
        check(java.util.Arrays.equals(org.omg.CORBA.UShortSeqHelper.read(osH.create_input_stream()), new short[]{(short) 4000}),
              "UShortSeqHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyH = orb.create_any();
        org.omg.CORBA.UShortSeqHelper.insert(anyH, new short[]{(short) 4000});
        check(java.util.Arrays.equals(org.omg.CORBA.UShortSeqHelper.extract(anyH), new short[]{(short) 4000}),
              "UShortSeqHelper.insert/extract Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
