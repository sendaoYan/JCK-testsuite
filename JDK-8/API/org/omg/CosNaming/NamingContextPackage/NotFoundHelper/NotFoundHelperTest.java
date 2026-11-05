/*
 * @test
 * @summary org.omg.CosNaming.NamingContextPackage.NotFoundHelper conformance: helper static id/type/narrow.
 * @run main NotFoundHelperTest
 */

public class NotFoundHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.NamingContextPackage.NotFoundHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.NamingContextPackage.NotFoundHelper.type() != null, "type() non-null");
        // ---- deepened (batch C, jdk8u372 probed): repository id + Any/CDR channels ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CosNaming.NamingContextPackage.NotFoundHelper.id().equals("IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"), "id() exact repository id");
        org.omg.CORBA.TypeCode tc = org.omg.CosNaming.NamingContextPackage.NotFoundHelper.type();
        check(tc.kind() == org.omg.CORBA.TCKind.tk_except, "type().kind() == tk_except");
        check(tc.id().equals(org.omg.CosNaming.NamingContextPackage.NotFoundHelper.id()), "type().id() equals Helper.id()");
        org.omg.CosNaming.NamingContextPackage.NotFound dpIn = new org.omg.CosNaming.NamingContextPackage.NotFound("wire-reason", org.omg.CosNaming.NamingContextPackage.NotFoundReason.not_object, new org.omg.CosNaming.NameComponent[0]);
        org.omg.CORBA.Any dpAny = orb.create_any();
        org.omg.CosNaming.NamingContextPackage.NotFoundHelper.insert(dpAny, dpIn);
        check(dpAny.type().kind() == org.omg.CORBA.TCKind.tk_except, "Any kind tk_except");
        org.omg.CosNaming.NamingContextPackage.NotFound dpOut = org.omg.CosNaming.NamingContextPackage.NotFoundHelper.extract(dpAny);
        check(dpOut.getMessage().startsWith("IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"), "Any round-trip message starts with repository id (reason not carried over CDR)");
        check(dpOut instanceof org.omg.CosNaming.NamingContextPackage.NotFound, "extract returns precise exception class");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        org.omg.CosNaming.NamingContextPackage.NotFoundHelper.write(dpOs, dpIn);
        org.omg.CosNaming.NamingContextPackage.NotFound dpFromStream = org.omg.CosNaming.NamingContextPackage.NotFoundHelper.read(dpOs.create_input_stream());
        check(dpFromStream.getMessage().equals("IDL:omg.org/CosNaming/NamingContext/NotFound:1.0"), "CDR round-trip message == repository id");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
