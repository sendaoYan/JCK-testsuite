/*
 * @test
 * @summary org.omg.PortableServer.POAHelper conformance: helper static id/type/narrow.
 * @run main POAHelperTest
 */

public class POAHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.POAHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.POAHelper.type() != null, "type() non-null");
        check(org.omg.PortableServer.POAHelper.narrow(null) == null, "narrow(null) returns null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
