/*
 * @test
 * @summary org.omg.CORBA.ServiceInformationHolder conformance: holder value field.
 * @run main ServiceInformationHolderTest
 */

public class ServiceInformationHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ServiceInformationHolder h0 = new org.omg.CORBA.ServiceInformationHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.CORBA.ServiceInformation v = null;
        org.omg.CORBA.ServiceInformationHolder h1 = new org.omg.CORBA.ServiceInformationHolder(v);
        check(h1.value == v, "value ctor stores value");
        // ---- deepened: Holder/Helper wire contract over offline ORB (jdk8u372 probed) ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(new org.omg.CORBA.ServiceInformationHolder().value == null, "default value is null");
        org.omg.CORBA.ServiceInformation si = new org.omg.CORBA.ServiceInformation(
                new int[]{7, 8}, new org.omg.CORBA.ServiceDetail[]{new org.omg.CORBA.ServiceDetail(5, new byte[]{1, 2, 3})});
        check(new org.omg.CORBA.ServiceInformationHolder(si).value == si, "ctor stores same reference");
        check(new org.omg.CORBA.ServiceInformationHolder()._type().kind() == org.omg.CORBA.TCKind.tk_struct,
              "ServiceInformationHolder._type().kind() == tk_struct");
        check(new org.omg.CORBA.ServiceInformationHolder()._type().equal(org.omg.CORBA.ServiceInformationHelper.type()),
              "ServiceInformationHolder._type().equal(ServiceInformationHelper.type())");
        org.omg.CORBA.portable.OutputStream osSi = orb.create_output_stream();
        new org.omg.CORBA.ServiceInformationHolder(si)._write(osSi);
        org.omg.CORBA.ServiceInformationHolder whSi = new org.omg.CORBA.ServiceInformationHolder();
        whSi._read(osSi.create_input_stream());
        check(java.util.Arrays.equals(whSi.value.service_options, new int[]{7, 8})
              && whSi.value.service_details[0].service_detail.length == 3,
              "CDR wire round-trip (options + nested detail)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
