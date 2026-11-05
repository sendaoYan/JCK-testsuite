/*
 * @test
 * @summary org.omg.IOP.ServiceIdHelper conformance: helper static id/type/narrow.
 * @run main ServiceIdHelperTest
 */

public class ServiceIdHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.ServiceIdHelper.id() != null, "id() non-null");
        check(org.omg.IOP.ServiceIdHelper.type() != null, "type() non-null");
        // ---- deepened (jdk8u372-b07 probed) ----
        check(org.omg.IOP.ServiceIdHelper.id().equals("IDL:omg.org/IOP/ServiceId:1.0"), "id() exact");
        check(org.omg.IOP.ServiceIdHelper.type().kind() == org.omg.CORBA.TCKind.tk_alias, "type().kind() == tk_alias");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.portable.OutputStream os = orb.create_output_stream();
        org.omg.IOP.ServiceIdHelper.write(os, org.omg.IOP.TransactionService.value);
        check(org.omg.IOP.ServiceIdHelper.read(os.create_input_stream()) == 0, "wire round-trip TransactionService 0");
        org.omg.CORBA.Any a = orb.create_any();
        org.omg.IOP.ServiceIdHelper.insert(a, org.omg.IOP.ExceptionDetailMessage.value);
        check(org.omg.IOP.ServiceIdHelper.extract(a) == 14, "Any insert/extract round-trip ExceptionDetailMessage 14");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
