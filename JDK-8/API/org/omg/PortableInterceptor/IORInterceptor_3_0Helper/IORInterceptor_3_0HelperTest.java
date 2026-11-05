/*
 * @test
 * @summary org.omg.PortableInterceptor.IORInterceptor_3_0Helper conformance: helper static id/type/narrow.
 * @run main IORInterceptor_3_0HelperTest
 */

public class IORInterceptor_3_0HelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.IORInterceptor_3_0Helper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.IORInterceptor_3_0Helper.type() != null, "type() non-null");
        check(org.omg.PortableInterceptor.IORInterceptor_3_0Helper.narrow(null) == null, "narrow(null) returns null");
        // ---- deepened (jdk8u372-b07 probed): objref helper ----
        check(org.omg.PortableInterceptor.IORInterceptor_3_0Helper.id().equals(
                  "IDL:omg.org/PortableInterceptor/IORInterceptor_3_0:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.IORInterceptor_3_0Helper.type().kind() == org.omg.CORBA.TCKind.tk_objref,
              "type().kind() == tk_objref");
        check(org.omg.PortableInterceptor.IORInterceptor_3_0Helper.narrow(null) == null, "narrow(null) == null");
        check(org.omg.PortableInterceptor.IORInterceptor_3_0Helper.unchecked_narrow(null) == null,
              "unchecked_narrow(null) == null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        boolean marshal = false;
        try { org.omg.PortableInterceptor.IORInterceptor_3_0Helper.extract(orb.create_any()); }
        catch (org.omg.CORBA.MARSHAL m) { marshal = true; }
        check(marshal, "extract(empty any) -> MARSHAL");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
