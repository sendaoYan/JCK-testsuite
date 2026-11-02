/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHelper conformance: helper static id/type/narrow.
 * @run main ObjectReferenceTemplateSeqHelperTest
 */

public class ObjectReferenceTemplateSeqHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
