/*
 * @test
 * @summary org.omg.CORBA.ULongSeqHelper conformance: helper static id/type/narrow.
 * @run main ULongSeqHelperTest
 */

public class ULongSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ULongSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ULongSeqHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.ULongSeqHelper.id().equals("IDL:omg.org/CORBA/ULongSeq:1.0"),
              "ULongSeqHelper.id() exact repository id");
        check(org.omg.CORBA.ULongSeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "ULongSeqHelper.type().kind() == tk_alias");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        org.omg.CORBA.ULongSeqHelper.write(osH, new int[]{2100000000});
        check(java.util.Arrays.equals(org.omg.CORBA.ULongSeqHelper.read(osH.create_input_stream()), new int[]{2100000000}),
              "ULongSeqHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyH = orb.create_any();
        org.omg.CORBA.ULongSeqHelper.insert(anyH, new int[]{2100000000});
        check(java.util.Arrays.equals(org.omg.CORBA.ULongSeqHelper.extract(anyH), new int[]{2100000000}),
              "ULongSeqHelper.insert/extract Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
