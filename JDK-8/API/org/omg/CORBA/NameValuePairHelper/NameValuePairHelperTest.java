/*
 * @test
 * @summary org.omg.CORBA.NameValuePairHelper conformance: helper static id/type/narrow.
 * @run main NameValuePairHelperTest
 */

public class NameValuePairHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.NameValuePairHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.NameValuePairHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.NameValuePairHelper.id().equals("IDL:omg.org/CORBA/NameValuePair:1.0"),
              "NameValuePairHelper.id() exact repository id");
        check(org.omg.CORBA.NameValuePairHelper.type().kind() == org.omg.CORBA.TCKind.tk_struct,
              "NameValuePairHelper.type().kind() == tk_struct");
        org.omg.CORBA.Any anyNv = orb.create_any();
        anyNv.insert_long(11);
        org.omg.CORBA.NameValuePair nv = new org.omg.CORBA.NameValuePair("k", anyNv);
        org.omg.CORBA.portable.OutputStream osNv = orb.create_output_stream();
        org.omg.CORBA.NameValuePairHelper.write(osNv, nv);
        org.omg.CORBA.NameValuePair rtNv = org.omg.CORBA.NameValuePairHelper.read(osNv.create_input_stream());
        check("k".equals(rtNv.id) && rtNv.value.extract_long() == 11,
              "write/read CDR round-trip (id + nested any value)");
        org.omg.CORBA.Any anyH = orb.create_any();
        org.omg.CORBA.NameValuePairHelper.insert(anyH, nv);
        org.omg.CORBA.NameValuePair xNv = org.omg.CORBA.NameValuePairHelper.extract(anyH);
        check("k".equals(xNv.id) && xNv.value.extract_long() == 11, "insert/extract Any round-trip");
        check(anyH.type().kind() == org.omg.CORBA.TCKind.tk_struct, "any kind is tk_struct after insert");
        check("k".equals(org.omg.CORBA.NameValuePairHelper.read(anyH.create_input_stream()).id),
              "Helper.read over any.create_input_stream() equals Helper.extract channel");
        try {
            org.omg.CORBA.Any anyBad = orb.create_any(); anyBad.insert_long(1); org.omg.CORBA.NameValuePairHelper.extract(anyBad);
            throw new AssertionError("NameValuePairHelper.extract over a tk_long any should throw MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "NameValuePairHelper.extract over a tk_long any -> MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
