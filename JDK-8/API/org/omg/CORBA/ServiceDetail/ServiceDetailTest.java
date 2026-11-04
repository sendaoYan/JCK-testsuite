/*
 * @test
 * @summary org.omg.CORBA.ServiceDetail conformance: value type fields and constructors.
 * @run main ServiceDetailTest
 */

public class ServiceDetailTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.CORBA.ServiceDetail s0 = new org.omg.CORBA.ServiceDetail();
        check(s0.service_detail_type == 0, "no-arg service_detail_type default");
        check(s0.service_detail == null, "no-arg service_detail default");
        org.omg.CORBA.ServiceDetail s1 = new org.omg.CORBA.ServiceDetail(7, null);
        check(s1.service_detail_type == 7, "ctor service_detail_type stored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
