/*
 * @test
 * @summary org.omg.PortableInterceptor.AdapterStateHelper conformance: helper static id/type/narrow.
 * @run main AdapterStateHelperTest
 */

public class AdapterStateHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.AdapterStateHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.AdapterStateHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed): alias helper (unsigned short) ----
        check(org.omg.PortableInterceptor.AdapterStateHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/AdapterState:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.AdapterStateHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "type().kind() == tk_alias");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.AdapterStateHelper.write(os, org.omg.PortableInterceptor.HOLDING.value);
        check(org.omg.PortableInterceptor.AdapterStateHelper.read(os.create_input_stream()) == (short) 0,
              "wire round-trip HOLDING state 0");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.AdapterStateHelper.insert(a, org.omg.PortableInterceptor.NON_EXISTENT.value);
        check(org.omg.PortableInterceptor.AdapterStateHelper.extract(a) == (short) 4,
              "Any round-trip NON_EXISTENT state 4");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
