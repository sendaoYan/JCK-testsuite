/*
 * @test
 * @summary org.omg.PortableServer.LifespanPolicyValue conformance: enumeration values.
 * @run main LifespanPolicyValueTest
 */

public class LifespanPolicyValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.LifespanPolicyValue.TRANSIENT != null, "TRANSIENT constant non-null");
        check(org.omg.PortableServer.LifespanPolicyValue.TRANSIENT.value() == org.omg.PortableServer.LifespanPolicyValue._TRANSIENT, "value() matches int constant");
        check(org.omg.PortableServer.LifespanPolicyValue.from_int(org.omg.PortableServer.LifespanPolicyValue._TRANSIENT) == org.omg.PortableServer.LifespanPolicyValue.TRANSIENT, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
