/*
 * @test
 * @summary org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper conformance: helper static id/type/narrow.
 * @run main DuplicateNameHelperTest
 */

public class DuplicateNameHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed) ----
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/ORBInitInfo/DuplicateName:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper.type().kind() == org.omg.CORBA.TCKind.tk_except,
              "type().kind() == tk_except");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper.write(os,
            new org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName("r", "Interceptor-1"));
        org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName rt =
            org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper.read(os.create_input_stream());
        check(rt.name.equals("Interceptor-1"), "wire round-trip preserves name member");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper.insert(a,
            new org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName("N"));
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper.extract(a).name.equals("N"),
              "Any round-trip preserves name");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
