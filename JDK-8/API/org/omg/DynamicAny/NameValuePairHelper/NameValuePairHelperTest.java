/*
 * @test
 * @summary org.omg.DynamicAny.NameValuePairHelper conformance: helper static id/type/narrow.
 * @run main NameValuePairHelperTest
 */

public class NameValuePairHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.NameValuePairHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.NameValuePairHelper.type() != null, "type() non-null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): struct typecode + dual wire channels ----
        check(org.omg.DynamicAny.NameValuePairHelper.id().equals("IDL:omg.org/DynamicAny/NameValuePair:1.0"), "id() exact");
        org.omg.CORBA.TypeCode dpTc = org.omg.DynamicAny.NameValuePairHelper.type();
        check(dpTc.kind() == org.omg.CORBA.TCKind.tk_struct, "type() tk_struct");
        check(dpTc.name().equals("NameValuePair"), "type().name() NameValuePair");
        check(dpTc.member_count() == 2, "member_count 2");
        check(dpTc.member_name(0).equals("id") && dpTc.member_name(1).equals("value"), "member names id/value");
        check(dpTc.member_type(0).kind() == org.omg.CORBA.TCKind.tk_alias
              && dpTc.member_type(0).name().equals("FieldName"),
              "member id is the FieldName ALIAS not plain tk_string");
        check(dpTc.member_type(1).kind() == org.omg.CORBA.TCKind.tk_any, "member value tk_any");
        org.omg.CORBA.Any dpV5 = orb.create_any(); dpV5.insert_long(5);
        org.omg.DynamicAny.NameValuePair dpN = new org.omg.DynamicAny.NameValuePair("n", dpV5);
        org.omg.CORBA.Any dpA = orb.create_any();
        org.omg.DynamicAny.NameValuePairHelper.insert(dpA, dpN);
        org.omg.DynamicAny.NameValuePair dpRt = org.omg.DynamicAny.NameValuePairHelper.extract(dpA);
        check(dpRt.id.equals("n") && dpRt.value.extract_long() == 5, "Any round-trip id + value");
        check(dpRt.value != dpV5, "round-trip Any is a fresh copy (wire copy semantics)");
        org.omg.CORBA.portable.OutputStream dpO = orb.create_output_stream();
        org.omg.DynamicAny.NameValuePairHelper.write(dpO, dpN);
        org.omg.DynamicAny.NameValuePair dpSr = org.omg.DynamicAny.NameValuePairHelper.read(dpO.create_input_stream());
        check(dpSr.id.equals("n") && dpSr.value.extract_long() == 5, "CDR stream round-trip id + value");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
