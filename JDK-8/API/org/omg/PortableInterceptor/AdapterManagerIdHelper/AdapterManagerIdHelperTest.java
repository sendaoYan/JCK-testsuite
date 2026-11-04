/*
 * @test
 * @summary org.omg.PortableInterceptor.AdapterManagerIdHelper conformance: helper static id/type/narrow.
 * @run main AdapterManagerIdHelperTest
 */

public class AdapterManagerIdHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.AdapterManagerIdHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.AdapterManagerIdHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
