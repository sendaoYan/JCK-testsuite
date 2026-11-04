/*
 * @test
 * @summary org.omg.PortableServer.POAPackage.WrongAdapterHelper conformance: helper static id/type/narrow.
 * @run main WrongAdapterHelperTest
 */

public class WrongAdapterHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.POAPackage.WrongAdapterHelper.id() != null, "id() non-null");
        check(org.omg.PortableServer.POAPackage.WrongAdapterHelper.type() != null, "type() non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
