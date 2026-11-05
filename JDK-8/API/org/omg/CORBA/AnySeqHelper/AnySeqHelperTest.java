/*
 * @test
 * @summary org.omg.CORBA.AnySeqHelper conformance: helper static id/type/narrow.
 * @run main AnySeqHelperTest
 */

public class AnySeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.AnySeqHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.AnySeqHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.AnySeqHelper.id().equals("IDL:omg.org/CORBA/AnySeq:1.0"),
              "AnySeqHelper.id() exact repository id");
        check(org.omg.CORBA.AnySeqHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "AnySeqHelper.type().kind() == tk_alias");
        check(org.omg.CORBA.AnySeqHelper.type().content_type().kind() == org.omg.CORBA.TCKind.tk_sequence,
              "AnySeqHelper.type().content_type().kind() == tk_sequence");
        org.omg.CORBA.Any a1 = orb.create_any();
        a1.insert_long(1);
        org.omg.CORBA.Any a2 = orb.create_any();
        a2.insert_string("x");
        org.omg.CORBA.portable.OutputStream osAs = orb.create_output_stream();
        org.omg.CORBA.AnySeqHelper.write(osAs, new org.omg.CORBA.Any[]{a1, a2});
        org.omg.CORBA.Any[] rtAs = org.omg.CORBA.AnySeqHelper.read(osAs.create_input_stream());
        check(rtAs.length == 2 && rtAs[0].extract_long() == 1 && "x".equals(rtAs[1].extract_string()),
              "AnySeq write/read CDR round-trip with mixed element types");
        org.omg.CORBA.Any anyAs = orb.create_any();
        org.omg.CORBA.AnySeqHelper.insert(anyAs, new org.omg.CORBA.Any[]{a1});
        org.omg.CORBA.Any[] xAs = org.omg.CORBA.AnySeqHelper.extract(anyAs);
        check(xAs.length == 1 && xAs[0].extract_long() == 1, "AnySeq insert/extract Any round-trip");
        check(anyAs.type().equal(org.omg.CORBA.AnySeqHelper.type()),
              "any.type().equal(AnySeqHelper.type()) after insert");
        try {
            org.omg.CORBA.Any anyBad = orb.create_any(); anyBad.insert_long(1); org.omg.CORBA.AnySeqHelper.extract(anyBad);
            throw new AssertionError("AnySeqHelper.extract over a tk_long any should throw MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "AnySeqHelper.extract over a tk_long any -> MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
