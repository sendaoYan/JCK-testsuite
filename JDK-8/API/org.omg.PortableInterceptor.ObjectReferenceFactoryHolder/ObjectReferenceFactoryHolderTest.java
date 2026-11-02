/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceFactoryHolder conformance: holder value field.
 * @run main ObjectReferenceFactoryHolderTest
 */

public class ObjectReferenceFactoryHolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.PortableInterceptor.ObjectReferenceFactoryHolder h0 = new org.omg.PortableInterceptor.ObjectReferenceFactoryHolder();
        check(h0.value == null, "no-arg default value");
        org.omg.PortableInterceptor.ObjectReferenceFactory v = null;
        org.omg.PortableInterceptor.ObjectReferenceFactoryHolder h1 = new org.omg.PortableInterceptor.ObjectReferenceFactoryHolder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
