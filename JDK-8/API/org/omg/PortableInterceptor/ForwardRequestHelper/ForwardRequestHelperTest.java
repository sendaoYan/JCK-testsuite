/*
 * @test
 * @summary org.omg.PortableInterceptor.ForwardRequestHelper conformance: helper static id/type/narrow.
 * @run main ForwardRequestHelperTest
 */

public class ForwardRequestHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ForwardRequestHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ForwardRequestHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed) ----
        check(org.omg.PortableInterceptor.ForwardRequestHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/ForwardRequest:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.ForwardRequestHelper.type().kind() == org.omg.CORBA.TCKind.tk_except,
              "type().kind() == tk_except");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.ForwardRequestHelper.write(os,
            new org.omg.PortableInterceptor.ForwardRequest("m", null));
        org.omg.PortableInterceptor.ForwardRequest rt = org.omg.PortableInterceptor.ForwardRequestHelper.read(os.create_input_stream());
        check(rt.forward == null, "wire round-trip forward field (null objref marshals as null)");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.ForwardRequestHelper.insert(a, new org.omg.PortableInterceptor.ForwardRequest(null));
        check(org.omg.PortableInterceptor.ForwardRequestHelper.extract(a).forward == null, "Any round-trip");
        check(a.type().kind() == org.omg.CORBA.TCKind.tk_except, "Any type after insert tk_except");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
