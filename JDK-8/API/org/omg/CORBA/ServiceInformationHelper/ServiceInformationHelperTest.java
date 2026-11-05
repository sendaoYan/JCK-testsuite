/*
 * @test
 * @summary org.omg.CORBA.ServiceInformationHelper conformance: helper static id/type/narrow.
 * @run main ServiceInformationHelperTest
 */

public class ServiceInformationHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.ServiceInformationHelper.id() != null, "id() non-null");
        check(org.omg.CORBA.ServiceInformationHelper.type() != null, "type() non-null");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CORBA.ServiceInformationHelper.id().equals("IDL:omg.org/CORBA/ServiceInformation:1.0"),
              "ServiceInformationHelper.id() exact repository id");
        check(org.omg.CORBA.ServiceInformationHelper.type().kind() == org.omg.CORBA.TCKind.tk_struct,
              "ServiceInformationHelper.type().kind() == tk_struct");
        org.omg.CORBA.ServiceInformation si = new org.omg.CORBA.ServiceInformation(
                new int[]{3, 4}, new org.omg.CORBA.ServiceDetail[]{new org.omg.CORBA.ServiceDetail(1, new byte[]{7})});
        org.omg.CORBA.portable.OutputStream osSi = orb.create_output_stream();
        org.omg.CORBA.ServiceInformationHelper.write(osSi, si);
        org.omg.CORBA.ServiceInformation rtSi = org.omg.CORBA.ServiceInformationHelper.read(osSi.create_input_stream());
        check(java.util.Arrays.equals(rtSi.service_options, new int[]{3, 4})
              && rtSi.service_details.length == 1 && rtSi.service_details[0].service_detail[0] == 7,
              "write/read CDR round-trip (options + nested detail bytes)");
        org.omg.CORBA.Any anySi = orb.create_any();
        org.omg.CORBA.ServiceInformationHelper.insert(anySi, si);
        check(java.util.Arrays.equals(org.omg.CORBA.ServiceInformationHelper.extract(anySi).service_options, new int[]{3, 4}),
              "insert/extract Any round-trip keeps options");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
