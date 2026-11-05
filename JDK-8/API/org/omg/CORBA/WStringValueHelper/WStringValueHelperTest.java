/*
 * @test
 * @summary org.omg.CORBA.WStringValueHelper conformance: helper static id/type/narrow.
 * @run main WStringValueHelperTest
 */

public class WStringValueHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.WStringValueHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.WStringValueHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.WStringValueHelper.id().equals("IDL:omg.org/CORBA/WStringValue:1.0"),
              "WStringValueHelper.id() exact repository id");
        check(new org.omg.CORBA.WStringValueHelper().get_id().equals("IDL:omg.org/CORBA/WStringValue:1.0"),
              "instance get_id() equals static id()");
        check(new org.omg.CORBA.WStringValueHelper() != null, "new WStringValueHelper() constructs");
        check(org.omg.CORBA.WStringValueHelper.type().kind() == org.omg.CORBA.TCKind.tk_value_box,
              "WStringValueHelper.type().kind() == tk_value_box");
        check(org.omg.CORBA.WStringValueHelper.type().content_type().kind() == org.omg.CORBA.TCKind.tk_wstring,
              "WStringValueHelper.type().content_type().kind() == tk_wstring");
        String[] wvCases = {"", "\u4f60\u597d", "a\nb"};
        for (String wvv : wvCases) {
            org.omg.CORBA.portable.OutputStream osWv = orb.create_output_stream();
            org.omg.CORBA.WStringValueHelper.write(osWv, wvv);
            check(org.omg.CORBA.WStringValueHelper.read(osWv.create_input_stream()).equals(wvv),
                  "WStringValueHelper write/read round-trip of [" + wvv.replace('\n', ' ') + "]");
        }
        org.omg.CORBA.portable.OutputStream osWvv = orb.create_output_stream();
        new org.omg.CORBA.WStringValueHelper().write_value(osWvv, "\u4f60\u597d");
        check("\u4f60\u597d".equals(new org.omg.CORBA.WStringValueHelper().read_value(osWvv.create_input_stream())),
              "write_value/read_value (BoxedValueHelper) wide round-trip");
        org.omg.CORBA.Any anyWv = orb.create_any();
        org.omg.CORBA.WStringValueHelper.insert(anyWv, "\u4f60\u597d");
        check("\u4f60\u597d".equals(org.omg.CORBA.WStringValueHelper.extract(anyWv)),
              "WStringValueHelper insert/extract Any round-trip (wide, non-Latin-1)");
        check(anyWv.type().kind() == org.omg.CORBA.TCKind.tk_value_box,
              "any kind is tk_value_box after WStringValueHelper.insert");
        try {
            anyWv.extract_wstring();
            throw new AssertionError("any.extract_wstring() after WStringValueHelper.insert (kind is tk_value_box, not tk_wstring) should throw BAD_OPERATION");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "any.extract_wstring() after WStringValueHelper.insert (kind is tk_value_box, not tk_wstring) -> BAD_OPERATION");
        }
        try {
            org.omg.CORBA.Any anyWv2 = orb.create_any(); anyWv2.insert_long(1); org.omg.CORBA.WStringValueHelper.extract(anyWv2);
            throw new AssertionError("WStringValueHelper.extract over a tk_long any should throw MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "WStringValueHelper.extract over a tk_long any -> MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
