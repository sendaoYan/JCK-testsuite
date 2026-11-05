/*
 * @test
 * @summary org.omg.CORBA.StringValueHelper conformance: helper static id/type/narrow.
 * @run main StringValueHelperTest
 */

public class StringValueHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.StringValueHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.StringValueHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.StringValueHelper.id().equals("IDL:omg.org/CORBA/StringValue:1.0"),
              "StringValueHelper.id() exact repository id");
        check(new org.omg.CORBA.StringValueHelper().get_id().equals("IDL:omg.org/CORBA/StringValue:1.0"),
              "instance get_id() equals static id()");
        check(new org.omg.CORBA.StringValueHelper() != null, "new StringValueHelper() constructs");
        check(org.omg.CORBA.StringValueHelper.type().kind() == org.omg.CORBA.TCKind.tk_value_box,
              "StringValueHelper.type().kind() == tk_value_box");
        check(org.omg.CORBA.StringValueHelper.type().content_type().kind() == org.omg.CORBA.TCKind.tk_string,
              "StringValueHelper.type().content_type().kind() == tk_string");
        String[] svCases = {"", "hello", "a\nb"};
        for (String svv : svCases) {
            org.omg.CORBA.portable.OutputStream osSv = orb.create_output_stream();
            org.omg.CORBA.StringValueHelper.write(osSv, svv);
            check(org.omg.CORBA.StringValueHelper.read(osSv.create_input_stream()).equals(svv),
                  "StringValueHelper write/read round-trip of [" + svv.replace('\n', ' ') + "]");
        }
        org.omg.CORBA.portable.OutputStream osVv = orb.create_output_stream();
        new org.omg.CORBA.StringValueHelper().write_value(osVv, "hello");
        check("hello".equals(new org.omg.CORBA.StringValueHelper().read_value(osVv.create_input_stream())),
              "write_value/read_value (BoxedValueHelper) round-trip");
        try {
            org.omg.CORBA.portable.OutputStream osNi = orb.create_output_stream(); org.omg.CORBA.StringValueHelper.write(osNi, "\u4f60\u597d");
            throw new AssertionError("StringValueHelper.write of non-Latin-1 (narrow code set ISO-8859-1) should throw DATA_CONVERSION");
        } catch (org.omg.CORBA.DATA_CONVERSION expected) {
            check(true, "StringValueHelper.write of non-Latin-1 (narrow code set ISO-8859-1) -> DATA_CONVERSION");
        }
        try {
            org.omg.CORBA.Any anySv = orb.create_any(); org.omg.CORBA.StringValueHelper.insert(anySv, "boxed");
            throw new AssertionError("StringValueHelper.insert offline (no boxed StringValue value class, jdk8u372) should throw MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "StringValueHelper.insert offline (no boxed StringValue value class, jdk8u372) -> MARSHAL");
        }
        try {
            org.omg.CORBA.Any anySv2 = orb.create_any(); anySv2.insert_long(1); org.omg.CORBA.StringValueHelper.extract(anySv2);
            throw new AssertionError("StringValueHelper.extract over a tk_long any should throw MARSHAL");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "StringValueHelper.extract over a tk_long any -> MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
