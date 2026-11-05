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

        // ---- deepened: full ctor readback and field mutability ----
        org.omg.CORBA.ServiceDetail withBytes = new org.omg.CORBA.ServiceDetail(9, new byte[]{1, 2, 3});
        check(withBytes.service_detail_type == 9, "ctor type readback");
        check(withBytes.service_detail != null && withBytes.service_detail.length == 3, "ctor byte array length");
        check(withBytes.service_detail[0] == 1 && withBytes.service_detail[2] == 3, "ctor byte contents");
        byte[] other = new byte[]{(byte) 0xFF};
        s1.service_detail = other;
        check(s1.service_detail == other, "service_detail field keeps assigned reference");
        s0.service_detail_type = 42;
        check(s0.service_detail_type == 42, "service_detail_type mutable");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.ServiceDetail.class),
              "ServiceDetail implements IDLEntity");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
