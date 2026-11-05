/*
 * @test
 * @summary org.omg.CORBA.LongSeqHelper conformance: helper static id/type/narrow.
 * @run main LongSeqHelperTest
 */

public class LongSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.LongSeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.LongSeqHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.LongSeqHelper.id().equals("IDL:omg.org/CORBA/LongSeq:1.0"),
              "LongSeqHelper.id() exact repository id");
        check(org.omg.CORBA.LongSeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "LongSeqHelper.type().kind() == tk_alias");
        org.omg.CORBA.portable.OutputStream osH = orb.create_output_stream();
        org.omg.CORBA.LongSeqHelper.write(osH, new int[]{-7, 0, 900});
        check(java.util.Arrays.equals(org.omg.CORBA.LongSeqHelper.read(osH.create_input_stream()), new int[]{-7, 0, 900}),
              "LongSeqHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyH = orb.create_any();
        org.omg.CORBA.LongSeqHelper.insert(anyH, new int[]{-7, 0, 900});
        check(java.util.Arrays.equals(org.omg.CORBA.LongSeqHelper.extract(anyH), new int[]{-7, 0, 900}),
              "LongSeqHelper.insert/extract Any round-trip");
        check(org.omg.CORBA.LongSeqHelper.type().content_type().kind() == org.omg.CORBA.TCKind.tk_sequence,
              "LongSeqHelper.type().content_type().kind() == tk_sequence (alias of sequence<long>)");
        try {
            org.omg.CORBA.Any anyNeg = orb.create_any(); anyNeg.insert_long(1); org.omg.CORBA.LongSeqHelper.extract(anyNeg);
            throw new AssertionError("LongSeqHelper.extract over a tk_long any should throw MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "LongSeqHelper.extract over a tk_long any -> MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
