/*
 * @test
 * @summary org.omg.PortableInterceptor.IORInterceptor_3_0Holder conformance: holder value field.
 * @run main IORInterceptor_3_0HolderTest
 */

public class IORInterceptor_3_0HolderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        org.omg.PortableInterceptor.IORInterceptor_3_0Holder h0 = new org.omg.PortableInterceptor.IORInterceptor_3_0Holder();
        check(h0.value == null, "no-arg default value");
        org.omg.PortableInterceptor.IORInterceptor_3_0 v = null;
        org.omg.PortableInterceptor.IORInterceptor_3_0Holder h1 = new org.omg.PortableInterceptor.IORInterceptor_3_0Holder(v);
        check(h1.value == v, "value ctor stores value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
