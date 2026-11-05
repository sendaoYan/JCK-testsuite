/*
 * @test
 * @summary org.omg.CORBA.IdentifierHelper conformance: helper static id/type/narrow.
 * @run main IdentifierHelperTest
 */

public class IdentifierHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.IdentifierHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.IdentifierHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.IdentifierHelper.id().equals("IDL:omg.org/CORBA/Identifier:1.0"),
              "IdentifierHelper.id() exact repository id");
        check(org.omg.CORBA.IdentifierHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "IdentifierHelper.type().kind() == tk_alias (typedef)");
        check(org.omg.CORBA.IdentifierHelper.type().content_type().kind() == org.omg.CORBA.TCKind.tk_string,
              "IdentifierHelper.type().content_type().kind() == tk_string");
        org.omg.CORBA.portable.OutputStream osA = orb.create_output_stream();
        org.omg.CORBA.IdentifierHelper.write(osA, "id1");
        check(org.omg.CORBA.IdentifierHelper.read(osA.create_input_stream()).equals("id1"),
              "IdentifierHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyA = orb.create_any();
        org.omg.CORBA.IdentifierHelper.insert(anyA, "id1");
        check(org.omg.CORBA.IdentifierHelper.extract(anyA).equals("id1"),
              "IdentifierHelper.insert/extract Any round-trip");
        check(anyA.extract_string().equals("id1"),
              "IdentifierHelper channel equals Any direct channel (extract_string)");
        check(anyA.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "any keeps the alias TypeCode after IdentifierHelper.insert");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
