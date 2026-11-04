/*
 * @test
 * @summary org.omg.PortableInterceptor.InvalidSlotHelper conformance: helper static id/type/narrow.
 * @run main InvalidSlotHelperTest
 */

public class InvalidSlotHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableInterceptor.InvalidSlotHelper.id() != null, "id() non-null");
        check(org.omg.PortableInterceptor.InvalidSlotHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
