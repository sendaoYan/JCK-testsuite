/*
 * @test
 * @summary org.omg.PortableServer.CurrentHelper conformance: helper static id/type/narrow.
 * @run main CurrentHelperTest
 */

public class CurrentHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.CurrentHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.CurrentHelper.type() != null, "type() non-null");
        check(org.omg.PortableServer.CurrentHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
