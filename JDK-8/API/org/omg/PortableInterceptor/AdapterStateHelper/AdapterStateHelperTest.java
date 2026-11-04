/*
 * @test
 * @summary org.omg.PortableInterceptor.AdapterStateHelper conformance: helper static id/type/narrow.
 * @run main AdapterStateHelperTest
 */

public class AdapterStateHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.AdapterStateHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.AdapterStateHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
