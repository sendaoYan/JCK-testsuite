/*
 * @test
 * @summary org.omg.PortableInterceptor.InvalidSlotHelper conformance: helper static id/type/narrow.
 * @run main InvalidSlotHelperTest
 */

public class InvalidSlotHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.InvalidSlotHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.InvalidSlotHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed) ----
        check(org.omg.PortableInterceptor.InvalidSlotHelper.id().equals(
                  "IDL:omg.org/PortableInterceptor/InvalidSlot:1.0"), "id() exact");
        check(org.omg.PortableInterceptor.InvalidSlotHelper.type().kind() == org.omg.CORBA.TCKind.tk_except,
              "type().kind() == tk_except");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.PortableInterceptor.InvalidSlotHelper.write(os, new org.omg.PortableInterceptor.InvalidSlot("m"));
        org.omg.PortableInterceptor.InvalidSlot rt = org.omg.PortableInterceptor.InvalidSlotHelper.read(os.create_input_stream());
        check(rt != null && rt.getClass() == org.omg.PortableInterceptor.InvalidSlot.class, "wire round-trip class");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.PortableInterceptor.InvalidSlotHelper.insert(a, new org.omg.PortableInterceptor.InvalidSlot());
        check(org.omg.PortableInterceptor.InvalidSlotHelper.extract(a) != null, "Any round-trip");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
