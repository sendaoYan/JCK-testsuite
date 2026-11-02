/*
 * @test
 * @summary org.omg.PortableServer.ServantRetentionPolicyValue conformance: enumeration values.
 * @run main ServantRetentionPolicyValueTest
 */

public class ServantRetentionPolicyValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN != null, "RETAIN constant non-null");
        check(org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN.value() == org.omg.PortableServer.ServantRetentionPolicyValue._RETAIN, "value() matches int constant");
        check(org.omg.PortableServer.ServantRetentionPolicyValue.from_int(org.omg.PortableServer.ServantRetentionPolicyValue._RETAIN) == org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
