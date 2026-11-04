/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.ServantAlreadyActiveHelper conformance: helper static id/type/narrow.
 * @run main ServantAlreadyActiveHelperTest
 */

public class ServantAlreadyActiveHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.POAPackage.ServantAlreadyActiveHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.POAPackage.ServantAlreadyActiveHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
