/*
 * @test
 * @summary org.omg.DynamicAny.NameValuePairSeqHelper conformance: helper static id/type/narrow.
 * @run main NameValuePairSeqHelperTest
 */

public class NameValuePairSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.NameValuePairSeqHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.NameValuePairSeqHelper.type() != null, "type() non-null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): alias chain + sequence wire ----
        check(org.omg.DynamicAny.NameValuePairSeqHelper.id().equals("IDL:omg.org/DynamicAny/NameValuePairSeq:1.0"), "id() exact");
        org.omg.CORBA.TypeCode dpTc = org.omg.DynamicAny.NameValuePairSeqHelper.type();
        check(dpTc.kind() == org.omg.CORBA.TCKind.tk_alias, "type() tk_alias");
        check(dpTc.name().equals("NameValuePairSeq"), "alias name NameValuePairSeq");
        check(dpTc.content_type().kind() == org.omg.CORBA.TCKind.tk_sequence, "alias content tk_sequence");
        check(dpTc.content_type().content_type().kind() == org.omg.CORBA.TCKind.tk_struct
              && dpTc.content_type().content_type().name().equals("NameValuePair"),
              "sequence element is the NameValuePair struct");
        org.omg.CORBA.Any dpV5 = orb.create_any(); dpV5.insert_long(5);
        org.omg.CORBA.Any dpV6 = orb.create_any(); dpV6.insert_string("six");
        org.omg.DynamicAny.NameValuePair[] dpArr = new org.omg.DynamicAny.NameValuePair[]{
            new org.omg.DynamicAny.NameValuePair("n1", dpV5), new org.omg.DynamicAny.NameValuePair("n2", dpV6)};
        org.omg.CORBA.Any dpA = orb.create_any();
        org.omg.DynamicAny.NameValuePairSeqHelper.insert(dpA, dpArr);
        org.omg.DynamicAny.NameValuePair[] dpRt = org.omg.DynamicAny.NameValuePairSeqHelper.extract(dpA);
        check(dpRt.length == 2, "Any round-trip length 2");
        check(dpRt[0].id.equals("n1") && dpRt[0].value.extract_long() == 5, "element 0 round-trip");
        check(dpRt[1].id.equals("n2") && dpRt[1].value.extract_string().equals("six"), "element 1 round-trip");
        org.omg.CORBA.portable.OutputStream dpO = orb.create_output_stream();
        org.omg.DynamicAny.NameValuePairSeqHelper.write(dpO, dpArr);
        org.omg.DynamicAny.NameValuePair[] dpSr = org.omg.DynamicAny.NameValuePairSeqHelper.read(dpO.create_input_stream());
        check(dpSr.length == 2 && dpSr[1].value.extract_string().equals("six"), "CDR round-trip");
        org.omg.CORBA.portable.OutputStream dpO0 = orb.create_output_stream();
        org.omg.DynamicAny.NameValuePairSeqHelper.write(dpO0, new org.omg.DynamicAny.NameValuePair[0]);
        check(org.omg.DynamicAny.NameValuePairSeqHelper.read(dpO0.create_input_stream()).length == 0, "empty sequence CDR round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
