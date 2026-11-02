/*
 * @test
 * @summary org.omg.PortableInterceptor.AdapterNameHelper conformance: helper static id/type/narrow.
 * @run main AdapterNameHelperTest
 */

public class AdapterNameHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.AdapterNameHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.AdapterNameHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
