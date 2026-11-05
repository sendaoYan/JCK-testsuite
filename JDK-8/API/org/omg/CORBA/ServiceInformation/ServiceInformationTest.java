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

        // ---- deepened: full ctor readback and field mutability ----
        org.omg.CORBA.ServiceDetail d = new org.omg.CORBA.ServiceDetail(5, new byte[]{9});
        org.omg.CORBA.ServiceInformation full = new org.omg.CORBA.ServiceInformation(new int[]{1, 2, 3},
                new org.omg.CORBA.ServiceDetail[]{d});
        check(full.service_options.length == 3, "ctor options length");
        check(full.service_options[0] == 1 && full.service_options[2] == 3, "ctor options contents");
        check(full.service_details.length == 1 && full.service_details[0] == d, "ctor details keep reference");
        s0.service_options = new int[]{7};
        s0.service_details = new org.omg.CORBA.ServiceDetail[]{d};
        check(s0.service_options[0] == 7, "service_options mutable");
        check(s0.service_details[0].service_detail_type == 5, "service_details mutable, nested readback");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.ServiceInformation.class),
              "ServiceInformation implements IDLEntity");
        check(java.lang.reflect.Modifier.isFinal(org.omg.CORBA.ServiceInformation.class.getModifiers()),
              "ServiceInformation is final");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
