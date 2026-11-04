/*
 * @test
 * @summary org.omg.IOP.ComponentIdHelper conformance: helper static id/type/narrow.
 * @run main ComponentIdHelperTest
 */

public class ComponentIdHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.ComponentIdHelper.id() != null, "id() non-null");
        check(org.omg.IOP.ComponentIdHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
