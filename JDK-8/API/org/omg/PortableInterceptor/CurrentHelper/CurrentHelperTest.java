/*
 * @test
 * @summary org.omg.PortableInterceptor.CurrentHelper conformance: helper static id/type/narrow.
 * @run main CurrentHelperTest
 */

public class CurrentHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.CurrentHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.CurrentHelper.type() != null, "type() non-null");
        check(org.omg.PortableInterceptor.CurrentHelper.narrow(null) == null, "narrow(null) returns null");
        // ---- deepened (jdk8u372-b07 probed): objref helper ----
        check(org.omg.PortableInterceptor.CurrentHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/Current:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.CurrentHelper.type().kind() == org.omg.CORBA.TCKind.tk_objref,
              "type().kind() == tk_objref");
        check(org.omg.PortableInterceptor.CurrentHelper.narrow(null) == null, "narrow(null) == null");
        check(org.omg.PortableInterceptor.CurrentHelper.unchecked_narrow(null) == null,
              "unchecked_narrow(null) == null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        boolean marshal = false;
        try { org.omg.PortableInterceptor.CurrentHelper.extract(orb.create_any()); }
        catch (org.omg.CORBA.MARSHAL m) { marshal = true; }
        check(marshal, "extract(empty any) -> MARSHAL");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
