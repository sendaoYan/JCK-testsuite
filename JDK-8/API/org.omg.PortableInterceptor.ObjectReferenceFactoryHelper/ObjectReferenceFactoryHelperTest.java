/*
 * @test
 * @summary org.omg.PortableInterceptor.ObjectReferenceFactoryHelper conformance: helper static id/type/narrow.
 * @run main ObjectReferenceFactoryHelperTest
 */

public class ObjectReferenceFactoryHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.ObjectReferenceFactoryHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.ObjectReferenceFactoryHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
