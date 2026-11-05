/*
 * @test
 * @summary org.omg.PortableInterceptor.AdapterManagerIdHelper conformance: helper static id/type/narrow.
 * @run main AdapterManagerIdHelperTest
 */

public class AdapterManagerIdHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.AdapterManagerIdHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.AdapterManagerIdHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): alias helper (unsigned long) ----
        check(org.omg.PortableInterceptor.AdapterManagerIdHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/AdapterManagerId:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.AdapterManagerIdHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "type().kind() == tk_alias");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.AdapterManagerIdHelper.write(os, 12345);
        check(org.omg.PortableInterceptor.AdapterManagerIdHelper.read(os.create_input_stream()) == 12345,
              "wire round-trip 12345");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.AdapterManagerIdHelper.insert(a, 99);
        check(org.omg.PortableInterceptor.AdapterManagerIdHelper.extract(a) == 99, "Any insert/extract round-trip");
        org.omg.CORBA.portable.OutputStream os2 = orb.create_output_stream();
        org.omg.PortableInterceptor.AdapterManagerIdHelper.write(os2, Integer.MAX_VALUE);
        check(org.omg.PortableInterceptor.AdapterManagerIdHelper.read(os2.create_input_stream()) == Integer.MAX_VALUE,
              "wire round-trip Integer.MAX_VALUE");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
