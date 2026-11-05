/*
 * @test
 * @summary org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper conformance: helper static id/type/narrow.
 * @run main InvalidNameHelperTest
 */

public class InvalidNameHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed) ----
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/ORBInitInfo/InvalidName:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper.type().kind() == org.omg.CORBA.TCKind.tk_except,
              "type().kind() == tk_except");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper.write(os,
            new org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName("x"));
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper.read(os.create_input_stream()) != null,
              "wire round-trip");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper.insert(a,
            new org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName());
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper.extract(a) != null, "Any round-trip");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
