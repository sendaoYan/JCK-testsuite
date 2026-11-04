/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.NoServantHelper conformance: helper static id/type/narrow.
 * @run main NoServantHelperTest
 */

public class NoServantHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.POAPackage.NoServantHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.POAPackage.NoServantHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
