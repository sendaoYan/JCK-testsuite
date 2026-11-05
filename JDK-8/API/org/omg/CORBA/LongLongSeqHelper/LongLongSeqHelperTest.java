/*
 * @test
 * @summary org.omg.CORBA.LongLongSeqHelper conformance: helper static id/type/narrow.
 * @run main LongLongSeqHelperTest
 */

public class LongLongSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.LongLongSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.LongLongSeqHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.LongLongSeqHelper.id().equals("IDL:omg.org/CORBA/LongLongSeq:1.0"),
              "LongLongSeqHelper.id() exact repository id");
        check(org.omg.CORBA.LongLongSeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "LongLongSeqHelper.type().kind() == tk_alias");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        org.omg.CORBA.LongLongSeqHelper.write(osH, new long[]{-1L});
        check(java.util.Arrays.equals(org.omg.CORBA.LongLongSeqHelper.read(osH.create_input_stream()), new long[]{-1L}),
              "LongLongSeqHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyH = orb.create_any();
        org.omg.CORBA.LongLongSeqHelper.insert(anyH, new long[]{-1L});
        check(java.util.Arrays.equals(org.omg.CORBA.LongLongSeqHelper.extract(anyH), new long[]{-1L}),
              "LongLongSeqHelper.insert/extract Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
