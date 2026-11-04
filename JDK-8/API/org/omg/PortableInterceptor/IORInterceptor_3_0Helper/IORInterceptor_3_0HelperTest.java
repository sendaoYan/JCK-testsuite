/*
 * @test
 * @summary org.omg.PortableInterceptor.IORInterceptor_3_0Helper conformance: helper static id/type/narrow.
 * @run main IORInterceptor_3_0HelperTest
 */

public class IORInterceptor_3_0HelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.IORInterceptor_3_0Helper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.IORInterceptor_3_0Helper.type() != null, "type() non-null");
        check(org.omg.PortableInterceptor.IORInterceptor_3_0Helper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
