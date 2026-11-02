/*
 * @test
 * @summary org.omg.PortableServer.POAManagerPackage.State conformance: enumeration values.
 * @run main StateTest
 */

public class StateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.POAManagerPackage.State.HOLDING != null, "HOLDING constant non-null");
        check(org.omg.PortableServer.POAManagerPackage.State.HOLDING.value() == org.omg.PortableServer.POAManagerPackage.State._HOLDING, "value() matches int constant");
        check(org.omg.PortableServer.POAManagerPackage.State.from_int(org.omg.PortableServer.POAManagerPackage.State._HOLDING) == org.omg.PortableServer.POAManagerPackage.State.HOLDING, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
