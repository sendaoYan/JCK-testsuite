/*
 * @test
 * @summary org.omg.PortableInterceptor.ServerIdHelper conformance: helper static id/type/narrow.
 * @run main ServerIdHelperTest
 */

public class ServerIdHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ServerIdHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ServerIdHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): alias helper (string) ----
        check(org.omg.PortableInterceptor.ServerIdHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/ServerId:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.ServerIdHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "type().kind() == tk_alias");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.ServerIdHelper.write(os, "srv");
        check(org.omg.PortableInterceptor.ServerIdHelper.read(os.create_input_stream()).equals("srv"),
              "wire round-trip server id");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.ServerIdHelper.insert(a, "srv2");
        check(org.omg.PortableInterceptor.ServerIdHelper.extract(a).equals("srv2"), "Any insert/extract round-trip");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
