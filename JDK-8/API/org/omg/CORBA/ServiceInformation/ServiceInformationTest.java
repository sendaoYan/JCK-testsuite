/*
 * @test
 * @summary org.omg.CORBA.ServiceInformation conformance: value type fields and constructors.
 * @run main ServiceInformationTest
 */

public class ServiceInformationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ServiceInformation s0 = new org.omg.CORBA.ServiceInformation();
        check(s0.service_options == null, "no-arg service_options default");
        check(s0.service_details == null, "no-arg service_details default");
        org.omg.CORBA.ServiceInformation s1 = new org.omg.CORBA.ServiceInformation(null, null);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
