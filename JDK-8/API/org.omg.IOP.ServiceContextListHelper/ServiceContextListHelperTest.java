/*
 * @test
 * @summary org.omg.IOP.ServiceContextListHelper conformance: helper static id/type/narrow.
 * @run main ServiceContextListHelperTest
 */

public class ServiceContextListHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.ServiceContextListHelper.id() != null, "id() non-null");
        check(org.omg.IOP.ServiceContextListHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
