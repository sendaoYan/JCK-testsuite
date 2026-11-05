/*
 * @test
 * @summary org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper conformance: helper static id/type/narrow.
 * @run main AdapterInactiveHelperTest
 */

public class AdapterInactiveHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): id exact / tk_except / Any + CDR wire round-trips ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.id().equals("IDL:omg.org/PortableServer/POAManager/AdapterInactive:1.0"), "id exact");
        check(org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.type().kind() == org.omg.CORBA.TCKind.tk_except, "type().kind() == tk_except");
        check(org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.type().id().equals("IDL:omg.org/PortableServer/POAManager/AdapterInactive:1.0"), "type().id() == id()");
        org.omg.CORBA.Any hAny = orb.create_any();
        org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.insert(hAny, new org.omg.PortableServer.POAManagerPackage.AdapterInactive("boom"));
        check(hAny.type().kind() == org.omg.CORBA.TCKind.tk_except, "Any kind tk_except after insert");
        check(org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.extract(hAny).getClass() == org.omg.PortableServer.POAManagerPackage.AdapterInactive.class, "Any insert/extract round-trip preserves class");
        check(org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.extract(hAny).getMessage().equals("IDL:omg.org/PortableServer/POAManager/AdapterInactive:1.0"), "Any round-trip drops user message");
        org.omg.CORBA.portable.OutputStream hos = orb.create_output_stream();
        org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.write(hos, new org.omg.PortableServer.POAManagerPackage.AdapterInactive("wire"));
        check(org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.read(hos.create_input_stream()).toString().equals("org.omg.PortableServer.POAManagerPackage.AdapterInactive: IDL:omg.org/PortableServer/POAManager/AdapterInactive:1.0"),
              "CDR write/read round-trip drops message");
        hos = orb.create_output_stream();
        org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.write(hos, new org.omg.PortableServer.POAManagerPackage.AdapterInactive("a"));
        org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.write(hos, new org.omg.PortableServer.POAManagerPackage.AdapterInactive("b"));
        org.omg.CORBA.portable.InputStream his = hos.create_input_stream();
        check(org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.read(his) != null && org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.read(his) != null,
              "two back-to-back exceptions in one CDR stream round-trip");
        try {
            org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper.extract(orb.create_any());
            check(false, "extract of empty Any must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "extract(empty Any) -> org.omg.CORBA.MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
