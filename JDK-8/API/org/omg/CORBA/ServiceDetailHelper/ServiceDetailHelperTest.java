/*
 * @test
 * @summary org.omg.CORBA.ServiceDetailHelper conformance: helper static id/type/narrow.
 * @run main ServiceDetailHelperTest
 */

public class ServiceDetailHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ServiceDetailHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ServiceDetailHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.ServiceDetailHelper.id().equals("IDL:omg.org/CORBA/ServiceDetail:1.0"),
              "ServiceDetailHelper.id() exact repository id");
        check(org.omg.CORBA.ServiceDetailHelper.type().kind() == org.omg.CORBA.TCKind.tk_struct,
              "ServiceDetailHelper.type().kind() == tk_struct");
        org.omg.CORBA.ServiceDetail sd = new org.omg.CORBA.ServiceDetail(9, new byte[]{1, 2, 3});
        org.omg.CORBA.portable.OutputStream osSd = orb.create_output_stream();
        org.omg.CORBA.ServiceDetailHelper.write(osSd, sd);
        org.omg.CORBA.ServiceDetail rtSd = org.omg.CORBA.ServiceDetailHelper.read(osSd.create_input_stream());
        check(rtSd.service_detail_type == 9 && java.util.Arrays.equals(rtSd.service_detail, new byte[]{1, 2, 3}),
              "write/read CDR round-trip (type + bytes)");
        org.omg.CORBA.Any anySd = orb.create_any();
        org.omg.CORBA.ServiceDetailHelper.insert(anySd, sd);
        check(org.omg.CORBA.ServiceDetailHelper.extract(anySd).service_detail_type == 9,
              "insert/extract Any round-trip keeps type");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
