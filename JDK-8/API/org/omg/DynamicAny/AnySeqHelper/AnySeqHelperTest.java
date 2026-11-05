/*
 * @test
 * @summary org.omg.DynamicAny.AnySeqHelper conformance: helper static id/type/narrow.
 * @run main AnySeqHelperTest
 */

public class AnySeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.AnySeqHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.AnySeqHelper.type() != null, "type() non-null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): alias chain + dual wire channels ----
        check(org.omg.DynamicAny.AnySeqHelper.id().equals("IDL:omg.org/DynamicAny/AnySeq:1.0"), "id() exact");
        org.omg.CORBA.TypeCode dpTc = org.omg.DynamicAny.AnySeqHelper.type();
        check(dpTc.kind() == org.omg.CORBA.TCKind.tk_alias, "type() tk_alias");
        check(dpTc.name().equals("AnySeq"), "alias name AnySeq");
        check(dpTc.content_type().kind() == org.omg.CORBA.TCKind.tk_sequence, "alias content tk_sequence");
        check(dpTc.content_type().content_type().kind() == org.omg.CORBA.TCKind.tk_any, "sequence element tk_any");
        org.omg.CORBA.Any dpE1 = orb.create_any(); dpE1.insert_long(1);
        org.omg.CORBA.Any dpE2 = orb.create_any(); dpE2.insert_string("two");
        org.omg.CORBA.Any dpA = orb.create_any();
        org.omg.DynamicAny.AnySeqHelper.insert(dpA, new org.omg.CORBA.Any[]{dpE1, dpE2});
        org.omg.CORBA.Any[] dpRt = org.omg.DynamicAny.AnySeqHelper.extract(dpA);
        check(dpRt.length == 2, "Any round-trip length 2");
        check(dpRt[0].extract_long() == 1, "element 0 long value");
        check(dpRt[1].extract_string().equals("two"), "element 1 string value");
        org.omg.CORBA.portable.OutputStream dpO = orb.create_output_stream();
        org.omg.DynamicAny.AnySeqHelper.write(dpO, new org.omg.CORBA.Any[]{dpE1, dpE2});
        org.omg.CORBA.Any[] dpSr = org.omg.DynamicAny.AnySeqHelper.read(dpO.create_input_stream());
        check(dpSr.length == 2 && dpSr[0].extract_long() == 1 && dpSr[1].extract_string().equals("two"),
              "CDR round-trip both elements");
        org.omg.CORBA.portable.OutputStream dpO0 = orb.create_output_stream();
        org.omg.DynamicAny.AnySeqHelper.write(dpO0, new org.omg.CORBA.Any[0]);
        check(org.omg.DynamicAny.AnySeqHelper.read(dpO0.create_input_stream()).length == 0, "empty sequence CDR round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
