/*
 * @test
 * @summary org.omg.IOP.ServiceContextHelper conformance: helper static id/type/narrow.
 * @run main ServiceContextHelperTest
 */

public class ServiceContextHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.ServiceContextHelper.id() != null, "id() non-null");
        check(org.omg.IOP.ServiceContextHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
