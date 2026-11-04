/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceTemplateHolder conformance: holder value field.
 * @run main ObjectReferenceTemplateHolderTest
 */

public class ObjectReferenceTemplateHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.PortableInterceptor.ObjectReferenceTemplateHolder h0 = new org.omg.PortableInterceptor.ObjectReferenceTemplateHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.PortableInterceptor.ObjectReferenceTemplate v = null;
        org.omg.PortableInterceptor.ObjectReferenceTemplateHolder h1 = new org.omg.PortableInterceptor.ObjectReferenceTemplateHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
