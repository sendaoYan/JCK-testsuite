/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.ServantNotActiveHelper conformance: helper static id/type/narrow.
 * @run main ServantNotActiveHelperTest
 */

public class ServantNotActiveHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.POAPackage.ServantNotActiveHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.POAPackage.ServantNotActiveHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
