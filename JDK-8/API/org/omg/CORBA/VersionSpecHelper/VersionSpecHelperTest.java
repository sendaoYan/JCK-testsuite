/*
 * @test
 * @summary org.omg.CORBA.VersionSpecHelper conformance: helper static id/type/narrow.
 * @run main VersionSpecHelperTest
 */

public class VersionSpecHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.VersionSpecHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.VersionSpecHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.VersionSpecHelper.id().equals("IDL:omg.org/CORBA/VersionSpec:1.0"),
              "VersionSpecHelper.id() exact repository id");
        check(org.omg.CORBA.VersionSpecHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "VersionSpecHelper.type().kind() == tk_alias (typedef)");
        check(org.omg.CORBA.VersionSpecHelper.type().content_type().kind() == org.omg.CORBA.TCKind.tk_string,
              "VersionSpecHelper.type().content_type().kind() == tk_string");
        org.omg.CORBA.portable.OutputStream osA = orb.create_output_stream();
        org.omg.CORBA.VersionSpecHelper.write(osA, "2.4");
        check(org.omg.CORBA.VersionSpecHelper.read(osA.create_input_stream()).equals("2.4"),
              "VersionSpecHelper.write/read CDR round-trip");
        org.omg.CORBA.Any anyA = orb.create_any();
        org.omg.CORBA.VersionSpecHelper.insert(anyA, "2.4");
        check(org.omg.CORBA.VersionSpecHelper.extract(anyA).equals("2.4"),
              "VersionSpecHelper.insert/extract Any round-trip");
        check(anyA.extract_string().equals("2.4"),
              "VersionSpecHelper channel equals Any direct channel (extract_string)");
        check(anyA.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "any keeps the alias TypeCode after VersionSpecHelper.insert");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
