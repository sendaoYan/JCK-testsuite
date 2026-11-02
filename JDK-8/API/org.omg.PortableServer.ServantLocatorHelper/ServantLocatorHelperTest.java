/*
 * @test
 * @summary org.omg.PortableServer.ServantLocatorHelper conformance: helper static id/type/narrow.
 * @run main ServantLocatorHelperTest
 */

public class ServantLocatorHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.ServantLocatorHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.ServantLocatorHelper.type() != null, "type() non-null");
        check(org.omg.PortableServer.ServantLocatorHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
