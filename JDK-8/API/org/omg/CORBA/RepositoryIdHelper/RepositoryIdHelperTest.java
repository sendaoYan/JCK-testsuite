/*
 * @test
 * @summary org.omg.CORBA.RepositoryIdHelper conformance: helper static id/type/narrow.
 * @run main RepositoryIdHelperTest
 */

public class RepositoryIdHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.RepositoryIdHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.RepositoryIdHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.RepositoryIdHelper.id().equals("IDL:omg.org/CORBA/RepositoryId:1.0"),
              "RepositoryIdHelper.id() exact repository id");
        check(org.omg.CORBA.RepositoryIdHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "RepositoryIdHelper.type().kind() == tk_alias (typedef)");
        check(org.omg.CORBA.RepositoryIdHelper.type().content_type().kind() == org.omg.CORBA.TCKind.tk_string,
              "RepositoryIdHelper.type().content_type().kind() == tk_string");
        org.omg.CORBA.portable.OutputStream osA = orb.create_output_stream();
        org.omg.CORBA.RepositoryIdHelper.write(osA, "IDL:a:1.0");
        check(org.omg.CORBA.RepositoryIdHelper.read(osA.create_input_stream()).equals("IDL:a:1.0"),
              "RepositoryIdHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyA = orb.create_any();
        org.omg.CORBA.RepositoryIdHelper.insert(anyA, "IDL:a:1.0");
        check(org.omg.CORBA.RepositoryIdHelper.extract(anyA).equals("IDL:a:1.0"),
              "RepositoryIdHelper.insert/extract Any round-trip");
        check(anyA.extract_string().equals("IDL:a:1.0"),
              "RepositoryIdHelper channel equals Any direct channel (extract_string)");
        check(anyA.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "any keeps the alias TypeCode after RepositoryIdHelper.insert");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
