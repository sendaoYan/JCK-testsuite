/*
 * @test
 * @summary org.omg.PortableInterceptor.ORBIdHelper conformance: helper static id/type/narrow.
 * @run main ORBIdHelperTest
 */

public class ORBIdHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ORBIdHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ORBIdHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): alias helper (string) ----
        check(org.omg.PortableInterceptor.ORBIdHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/ORBId:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.ORBIdHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "type().kind() == tk_alias");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.ORBIdHelper.write(os, "myorb");
        check(org.omg.PortableInterceptor.ORBIdHelper.read(os.create_input_stream()).equals("myorb"),
              "wire round-trip orb id");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.ORBIdHelper.insert(a, "orb-x");
        check(org.omg.PortableInterceptor.ORBIdHelper.extract(a).equals("orb-x"), "Any insert/extract round-trip");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
