/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtPackage.StringNameHelper conformance: helper static id/type/narrow.
 * @run main StringNameHelperTest
 */

public class StringNameHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NamingContextExtPackage.StringNameHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NamingContextExtPackage.StringNameHelper.type() != null, "type() non-null");
        // ---- deepened (batch C, jdk8u372 probed): string typedef helper dual channels ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CosNaming.NamingContextExtPackage.StringNameHelper.id().equals("IDL:omg.org/CosNaming/NamingContextExt/StringName:1.0"), "id() exact");
        org.omg.CORBA.TypeCode tc = org.omg.CosNaming.NamingContextExtPackage.StringNameHelper.type();
        check(tc.kind() == org.omg.CORBA.TCKind.tk_alias, "type().kind() == tk_alias");
        check(tc.name().equals("StringName"), "alias name StringName");
        check(tc.content_type().kind() == org.omg.CORBA.TCKind.tk_string, "content type tk_string");
        org.omg.CORBA.Any any = orb.create_any();
        org.omg.CosNaming.NamingContextExtPackage.StringNameHelper.insert(any, "val1");
        check(any.type().kind() == org.omg.CORBA.TCKind.tk_alias, "Any kind tk_alias");
        check(org.omg.CosNaming.NamingContextExtPackage.StringNameHelper.extract(any).equals("val1"), "Any round-trip");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        org.omg.CosNaming.NamingContextExtPackage.StringNameHelper.write(dpOs, "val2");
        check(org.omg.CosNaming.NamingContextExtPackage.StringNameHelper.read(dpOs.create_input_stream()).equals("val2"), "CDR round-trip");
        org.omg.CORBA.Any anyEmpty = orb.create_any();
        org.omg.CosNaming.NamingContextExtPackage.StringNameHelper.insert(anyEmpty, "");
        check(org.omg.CosNaming.NamingContextExtPackage.StringNameHelper.extract(anyEmpty).isEmpty(), "empty string Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
