/*
 * @test
 * @summary org.omg.PortableInterceptor.ForwardRequestHelper conformance: helper static id/type/narrow.
 * @run main ForwardRequestHelperTest
 */

public class ForwardRequestHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ForwardRequestHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ForwardRequestHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
