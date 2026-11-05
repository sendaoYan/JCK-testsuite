/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.CannotProceedHelper conformance: helper static id/type/narrow.
 * @run main CannotProceedHelperTest
 */

public class CannotProceedHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.type() != null, "type() non-null");
        // ---- deepened (batch C, jdk8u372 probed): repository id + Any/CDR channels ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.id().equals("IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"), "id() exact repository id");
        org.omg.CORBA.TypeCode tc = org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.type();
        check(tc.kind() == org.omg.CORBA.TCKind.tk_except, "type().kind() == tk_except");
        check(tc.id().equals(org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.id()), "type().id() equals Helper.id()");
        org.omg.CosNaming.NamingContextPackage.CannotProceed dpIn = new org.omg.CosNaming.NamingContextPackage.CannotProceed("wire-reason", null, new org.omg.CosNaming.NameComponent[0]);
        org.omg.CORBA.Any dpAny = orb.create_any();
        org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.insert(dpAny, dpIn);
        check(dpAny.type().kind() == org.omg.CORBA.TCKind.tk_except, "Any kind tk_except");
        org.omg.CosNaming.NamingContextPackage.CannotProceed dpOut = org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.extract(dpAny);
        check(dpOut.getMessage().startsWith("IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"), "Any round-trip message starts with repository id (reason not carried over CDR)");
        check(dpOut instanceof org.omg.CosNaming.NamingContextPackage.CannotProceed, "extract returns precise exception class");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.write(dpOs, dpIn);
        org.omg.CosNaming.NamingContextPackage.CannotProceed dpFromStream = org.omg.CosNaming.NamingContextPackage.CannotProceedHelper.read(dpOs.create_input_stream());
        check(dpFromStream.getMessage().equals("IDL:omg.org/CosNaming/NamingContext/CannotProceed:1.0"), "CDR round-trip message == repository id");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
