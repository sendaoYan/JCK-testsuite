/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceTemplateHelper conformance: helper static id/type/narrow.
 * @run main ObjectReferenceTemplateHelperTest
 */

public class ObjectReferenceTemplateHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ObjectReferenceTemplateHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ObjectReferenceTemplateHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
