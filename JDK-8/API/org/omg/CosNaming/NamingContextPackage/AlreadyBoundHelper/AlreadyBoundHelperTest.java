/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper conformance: helper static id/type/narrow.
 * @run main AlreadyBoundHelperTest
 */

public class AlreadyBoundHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.type() != null, "type() non-null");
        // ---- deepened (batch C, jdk8u372 probed): repository id + Any/CDR channels ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.id().equals("IDL:omg.org/CosNaming/NamingContext/AlreadyBound:1.0"), "id() exact repository id");
        org.omg.CORBA.TypeCode tc = org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.type();
        check(tc.kind() == org.omg.CORBA.TCKind.tk_except, "type().kind() == tk_except");
        check(tc.id().equals(org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.id()), "type().id() equals Helper.id()");
        org.omg.CosNaming.NamingContextPackage.AlreadyBound dpIn = new org.omg.CosNaming.NamingContextPackage.AlreadyBound("wire-reason");
        org.omg.CORBA.Any dpAny = orb.create_any();
        org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.insert(dpAny, dpIn);
        check(dpAny.type().kind() == org.omg.CORBA.TCKind.tk_except, "Any kind tk_except");
        org.omg.CosNaming.NamingContextPackage.AlreadyBound dpOut = org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.extract(dpAny);
        check(dpOut.getMessage().startsWith("IDL:omg.org/CosNaming/NamingContext/AlreadyBound:1.0"), "Any round-trip message starts with repository id (reason not carried over CDR)");
        check(dpOut instanceof org.omg.CosNaming.NamingContextPackage.AlreadyBound, "extract returns precise exception class");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.write(dpOs, dpIn);
        org.omg.CosNaming.NamingContextPackage.AlreadyBound dpFromStream = org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.read(dpOs.create_input_stream());
        check(dpFromStream.getMessage().equals("IDL:omg.org/CosNaming/NamingContext/AlreadyBound:1.0"), "CDR round-trip message == repository id");
        org.omg.CORBA.Any dpAnyD = orb.create_any();
        org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.insert(dpAnyD, new org.omg.CosNaming.NamingContextPackage.AlreadyBound());
        check(org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper.extract(dpAnyD) instanceof org.omg.CosNaming.NamingContextPackage.AlreadyBound, "default exception Any round-trip class preserved");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
