/*
 * @test
 * @summary org.omg.CORBA.ShortSeqHelper conformance: helper static id/type/narrow.
 * @run main ShortSeqHelperTest
 */

public class ShortSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ShortSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ShortSeqHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.ShortSeqHelper.id().equals("IDL:omg.org/CORBA/ShortSeq:1.0"),
              "ShortSeqHelper.id() exact repository id");
        check(org.omg.CORBA.ShortSeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "ShortSeqHelper.type().kind() == tk_alias");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        org.omg.CORBA.ShortSeqHelper.write(osH, new short[]{(short) 7});
        check(java.util.Arrays.equals(org.omg.CORBA.ShortSeqHelper.read(osH.create_input_stream()), new short[]{(short) 7}),
              "ShortSeqHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyH = orb.create_any();
        org.omg.CORBA.ShortSeqHelper.insert(anyH, new short[]{(short) 7});
        check(java.util.Arrays.equals(org.omg.CORBA.ShortSeqHelper.extract(anyH), new short[]{(short) 7}),
              "ShortSeqHelper.insert/extract Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
