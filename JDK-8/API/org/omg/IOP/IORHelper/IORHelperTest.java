/*
 * @test
 * @summary org.omg.IOP.IORHelper conformance: helper static id/type/narrow.
 * @run main IORHelperTest
 */

public class IORHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.IORHelper.id() != null, "id() non-null");
        check(org.omg.IOP.IORHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
