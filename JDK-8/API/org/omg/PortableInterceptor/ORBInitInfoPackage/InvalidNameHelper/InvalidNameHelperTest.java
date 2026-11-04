/*
 * @test
 * @summary org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper conformance: helper static id/type/narrow.
 * @run main InvalidNameHelperTest
 */

public class InvalidNameHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
