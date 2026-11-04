/*
 * @test
 * @summary org.omg.PortableServer.CurrentPackage.NoContextHelper conformance: helper static id/type/narrow.
 * @run main NoContextHelperTest
 */

public class NoContextHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.CurrentPackage.NoContextHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.CurrentPackage.NoContextHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
