/*
 * @test
 * @summary org.omg.CORBA.FloatSeqHelper conformance: helper static id/type/narrow.
 * @run main FloatSeqHelperTest
 */

public class FloatSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.FloatSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.FloatSeqHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.FloatSeqHelper.id().equals("IDL:omg.org/CORBA/FloatSeq:1.0"),
              "FloatSeqHelper.id() exact repository id");
        check(org.omg.CORBA.FloatSeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "FloatSeqHelper.type().kind() == tk_alias");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        org.omg.CORBA.FloatSeqHelper.write(osH, new float[]{2.5f});
        check(java.util.Arrays.equals(org.omg.CORBA.FloatSeqHelper.read(osH.create_input_stream()), new float[]{2.5f}),
              "FloatSeqHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyH = orb.create_any();
        org.omg.CORBA.FloatSeqHelper.insert(anyH, new float[]{2.5f});
        check(java.util.Arrays.equals(org.omg.CORBA.FloatSeqHelper.extract(anyH), new float[]{2.5f}),
              "FloatSeqHelper.insert/extract Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
