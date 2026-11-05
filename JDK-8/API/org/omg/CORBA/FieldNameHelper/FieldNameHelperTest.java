/*
 * @test
 * @summary org.omg.CORBA.FieldNameHelper conformance: helper static id/type/narrow.
 * @run main FieldNameHelperTest
 */

public class FieldNameHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.FieldNameHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.FieldNameHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.FieldNameHelper.id().equals("IDL:omg.org/CORBA/FieldName:1.0"),
              "FieldNameHelper.id() exact repository id");
        check(org.omg.CORBA.FieldNameHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "FieldNameHelper.type().kind() == tk_alias (typedef)");
        check(org.omg.CORBA.FieldNameHelper.type().content_type().kind() == org.omg.CORBA.TCKind.tk_string,
              "FieldNameHelper.type().content_type().kind() == tk_string");
        org.omg.CORBA.portable.OutputStream osA = orb.create_output_stream();
        org.omg.CORBA.FieldNameHelper.write(osA, "f1");
        check(org.omg.CORBA.FieldNameHelper.read(osA.create_input_stream()).equals("f1"),
              "FieldNameHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyA = orb.create_any();
        org.omg.CORBA.FieldNameHelper.insert(anyA, "f1");
        check(org.omg.CORBA.FieldNameHelper.extract(anyA).equals("f1"),
              "FieldNameHelper.insert/extract Any round-trip");
        check(anyA.extract_string().equals("f1"),
              "FieldNameHelper channel equals Any direct channel (extract_string)");
        check(anyA.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "any keeps the alias TypeCode after FieldNameHelper.insert");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
