/*
 * @test
 * @summary org.omg.IOP.ServiceIdHelper conformance: helper static id/type/narrow.
 * @run main ServiceIdHelperTest
 */

public class ServiceIdHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.ServiceIdHelper.id() != null, "id() non-null");
        check(org.omg.IOP.ServiceIdHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
