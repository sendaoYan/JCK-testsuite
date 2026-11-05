/*
 * @test
 * @summary org.omg.CORBA.VisibilityHelper conformance: helper static id/type/narrow.
 * @run main VisibilityHelperTest
 */

public class VisibilityHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.VisibilityHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.VisibilityHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.VisibilityHelper.id().equals("IDL:omg.org/CORBA/Visibility:1.0"),
              "VisibilityHelper.id() exact repository id");
        check(org.omg.CORBA.VisibilityHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "VisibilityHelper.type().kind() == tk_alias (typedef)");
        check(org.omg.CORBA.VisibilityHelper.type().content_type().kind() == org.omg.CORBA.TCKind.tk_short,
              "VisibilityHelper.type().content_type().kind() == tk_short");
        org.omg.CORBA.portable.OutputStream osA = orb.create_output_stream();
        org.omg.CORBA.VisibilityHelper.write(osA, (short) 1);
        check(org.omg.CORBA.VisibilityHelper.read(osA.create_input_stream()) == (short) 1,
              "VisibilityHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyA = orb.create_any();
        org.omg.CORBA.VisibilityHelper.insert(anyA, (short) 1);
        check(org.omg.CORBA.VisibilityHelper.extract(anyA) == (short) 1,
              "VisibilityHelper.insert/extract Any round-trip");
        check(anyA.extract_short() == (short) 1,
              "VisibilityHelper channel equals Any direct channel (extract_short)");
        check(anyA.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "any keeps the alias TypeCode after VisibilityHelper.insert");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
