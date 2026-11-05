/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper conformance: helper static id/type/narrow.
 * @run main NotFoundReasonHelperTest
 */

public class NotFoundReasonHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.type() != null, "type() non-null");
        // ---- deepened (batch C, jdk8u372 probed): enum helper full table over wire ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.id().equals("IDL:omg.org/CosNaming/NamingContext/NotFoundReason:1.0"), "id() exact");
        org.omg.CORBA.TypeCode tc = org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.type();
        check(tc.kind() == org.omg.CORBA.TCKind.tk_enum, "type().kind() == tk_enum");
        check(tc.member_count() == 3, "enum member_count == 3");
        check(tc.member_name(0).equals("missing_node") && tc.member_name(1).equals("not_context")
              && tc.member_name(2).equals("not_object"), "enum member names in order");
        org.omg.CosNaming.NamingContextPackage.NotFoundReason[] all = {
            org.omg.CosNaming.NamingContextPackage.NotFoundReason.missing_node,
            org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_context,
            org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_object};
        for (org.omg.CosNaming.NamingContextPackage.NotFoundReason k : all) {
            org.omg.CORBA.Any a = orb.create_any();
            org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.insert(a, k);
            check(org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.extract(a) == k,
                  "Any round-trip normalizes to SAME constant (value " + k.value() + ")");
            org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
            org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.write(dpOs, k);
            check(org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper.read(dpOs.create_input_stream()) == k,
                  "CDR round-trip normalizes to SAME constant (value " + k.value() + ")");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
