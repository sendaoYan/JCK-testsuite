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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
