/*
 * @test
 * @summary org.omg.IOP.ServiceContextHelper conformance: helper static id/type/narrow.
 * @run main ServiceContextHelperTest
 */

public class ServiceContextHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.ServiceContextHelper.id() != null, "id() non-null");
        check(org.omg.IOP.ServiceContextHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed) ----
        check(org.omg.IOP.ServiceContextHelper.id().equals("IDL:omg.org/IOP/ServiceContext:1.0"), "id() exact");
        check(org.omg.IOP.ServiceContextHelper.type().kind() == org.omg.CORBA.TCKind.tk_struct, "type tk_struct");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.ServiceContextHelper.write(os, new org.omg.IOP.ServiceContext(0xEF, new byte[]{(byte) 0xAA}));
        org.omg.IOP.ServiceContext rt = org.omg.IOP.ServiceContextHelper.read(os.create_input_stream());
        check(rt.context_id == 0xEF && rt.context_data[0] == (byte) 0xAA, "wire round-trip");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.IOP.ServiceContextHelper.insert(a, new org.omg.IOP.ServiceContext(4, new byte[]{1, 2}));
        check(org.omg.IOP.ServiceContextHelper.extract(a).context_id == 4, "Any insert/extract round-trip");
        boolean npe = false;
        try { org.omg.IOP.ServiceContextHelper.insert(a, null); } catch (NullPointerException n) { npe = true; }
        check(npe, "insert(any, null) -> NullPointerException");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
