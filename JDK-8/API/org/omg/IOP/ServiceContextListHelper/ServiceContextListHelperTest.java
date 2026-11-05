/*
 * @test
 * @summary org.omg.IOP.ServiceContextListHelper conformance: helper static id/type/narrow.
 * @run main ServiceContextListHelperTest
 */

public class ServiceContextListHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.ServiceContextListHelper.id() != null, "id() non-null");
        check(org.omg.IOP.ServiceContextListHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed) ----
        check(org.omg.IOP.ServiceContextListHelper.id().equals("IDL:omg.org/IOP/ServiceContextList:1.0"), "id() exact");
        check(org.omg.IOP.ServiceContextListHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias,
              "type().kind() == tk_alias (sequence typedef)");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.IOP.ServiceContext[] scl = {
            new org.omg.IOP.ServiceContext(1, new byte[]{1}),
            new org.omg.IOP.ServiceContext(2, new byte[]{2, 2})};
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.ServiceContextListHelper.write(os, scl);
        org.omg.IOP.ServiceContext[] rt = org.omg.IOP.ServiceContextListHelper.read(os.create_input_stream());
        check(rt.length == 2 && rt[0].context_id == 1 && rt[1].context_id == 2 && rt[1].context_data.length == 2,
              "wire round-trip sequence of 2 with per-element bytes");
        org.omg.CORBA.portable.OutputStream osE = orb.create_output_stream();
        org.omg.IOP.ServiceContextListHelper.write(osE, new org.omg.IOP.ServiceContext[0]);
        check(org.omg.IOP.ServiceContextListHelper.read(osE.create_input_stream()).length == 0,
              "empty sequence wire round-trip length 0");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.IOP.ServiceContextListHelper.insert(a, scl);
        check(org.omg.IOP.ServiceContextListHelper.extract(a).length == 2, "Any insert/extract round-trip");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
