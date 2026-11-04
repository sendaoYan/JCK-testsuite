/*
 * @test
 * @summary org.omg.PortableInterceptor.ServerIdHelper conformance: helper static id/type/narrow.
 * @run main ServerIdHelperTest
 */

public class ServerIdHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ServerIdHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ServerIdHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
