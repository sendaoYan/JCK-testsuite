/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtPackage.AddressHelper conformance: helper static id/type/narrow.
 * @run main AddressHelperTest
 */

public class AddressHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NamingContextExtPackage.AddressHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NamingContextExtPackage.AddressHelper.type() != null, "type() non-null");
        // ---- deepened (batch C, jdk8u372 probed): string typedef helper dual channels ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CosNaming.NamingContextExtPackage.AddressHelper.id().equals("IDL:omg.org/CosNaming/NamingContextExt/Address:1.0"), "id() exact");
        org.omg.CORBA.TypeCode tc = org.omg.CosNaming.NamingContextExtPackage.AddressHelper.type();
        check(tc.kind() == org.omg.CORBA.TCKind.tk_alias, "type().kind() == tk_alias");
        check(tc.name().equals("Address"), "alias name Address");
        check(tc.content_type().kind() == org.omg.CORBA.TCKind.tk_string, "content type tk_string");
        org.omg.CORBA.Any any = orb.create_any();
        org.omg.CosNaming.NamingContextExtPackage.AddressHelper.insert(any, "val1");
        check(any.type().kind() == org.omg.CORBA.TCKind.tk_alias, "Any kind tk_alias");
        check(org.omg.CosNaming.NamingContextExtPackage.AddressHelper.extract(any).equals("val1"), "Any round-trip");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        org.omg.CosNaming.NamingContextExtPackage.AddressHelper.write(dpOs, "val2");
        check(org.omg.CosNaming.NamingContextExtPackage.AddressHelper.read(dpOs.create_input_stream()).equals("val2"), "CDR round-trip");
        org.omg.CORBA.Any anyEmpty = orb.create_any();
        org.omg.CosNaming.NamingContextExtPackage.AddressHelper.insert(anyEmpty, "");
        check(org.omg.CosNaming.NamingContextExtPackage.AddressHelper.extract(anyEmpty).isEmpty(), "empty string Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
