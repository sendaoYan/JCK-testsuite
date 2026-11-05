/*
 * @test
 * @summary org.omg.PortableInterceptor.AdapterNameHelper conformance: helper static id/type/narrow.
 * @run main AdapterNameHelperTest
 */

public class AdapterNameHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.AdapterNameHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.AdapterNameHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): sequence-of-string alias helper ----
        check(org.omg.PortableInterceptor.AdapterNameHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/AdapterName:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.AdapterNameHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "type().kind() == tk_alias");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        String[] an = {"a", "b"};
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.AdapterNameHelper.write(os, an);
        String[] rt = org.omg.PortableInterceptor.AdapterNameHelper.read(os.create_input_stream());
        check(rt.length == 2 && rt[0].equals("a") && rt[1].equals("b"), "wire round-trip 2-element name path");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.AdapterNameHelper.insert(a, an);
        check(org.omg.PortableInterceptor.AdapterNameHelper.extract(a).length == 2, "Any insert/extract round-trip");
        org.omg.CORBA.portable.OutputStream osE = orb.create_output_stream();
        org.omg.PortableInterceptor.AdapterNameHelper.write(osE, new String[0]);
        check(org.omg.PortableInterceptor.AdapterNameHelper.read(osE.create_input_stream()).length == 0,
              "empty name path wire round-trip");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
