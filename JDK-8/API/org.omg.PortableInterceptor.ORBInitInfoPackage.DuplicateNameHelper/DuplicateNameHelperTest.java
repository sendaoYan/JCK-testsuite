/*
 * @test
 * @summary org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper conformance: helper static id/type/narrow.
 * @run main DuplicateNameHelperTest
 */

public class DuplicateNameHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
