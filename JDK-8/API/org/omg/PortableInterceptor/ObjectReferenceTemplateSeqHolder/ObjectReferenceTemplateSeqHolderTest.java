/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder conformance: holder value field.
 * @run main ObjectReferenceTemplateSeqHolderTest
 */

public class ObjectReferenceTemplateSeqHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder h0 = new org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.PortableInterceptor.ObjectReferenceTemplate[] v = null;
        org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder h1 = new org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
