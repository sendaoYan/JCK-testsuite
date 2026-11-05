/*
 * @test
 * @summary org.omg.CORBA.ULongLongSeqHelper conformance: helper static id/type/narrow.
 * @run main ULongLongSeqHelperTest
 */

public class ULongLongSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ULongLongSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ULongLongSeqHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.ULongLongSeqHelper.id().equals("IDL:omg.org/CORBA/ULongLongSeq:1.0"),
              "ULongLongSeqHelper.id() exact repository id");
        check(org.omg.CORBA.ULongLongSeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "ULongLongSeqHelper.type().kind() == tk_alias");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        org.omg.CORBA.ULongLongSeqHelper.write(osH, new long[]{123456789012345L});
        check(java.util.Arrays.equals(org.omg.CORBA.ULongLongSeqHelper.read(osH.create_input_stream()), new long[]{123456789012345L}),
              "ULongLongSeqHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyH = orb.create_any();
        org.omg.CORBA.ULongLongSeqHelper.insert(anyH, new long[]{123456789012345L});
        check(java.util.Arrays.equals(org.omg.CORBA.ULongLongSeqHelper.extract(anyH), new long[]{123456789012345L}),
              "ULongLongSeqHelper.insert/extract Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
