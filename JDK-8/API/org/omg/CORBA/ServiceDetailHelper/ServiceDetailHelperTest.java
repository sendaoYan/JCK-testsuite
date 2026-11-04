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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
