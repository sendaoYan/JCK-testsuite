/*
 * @test
 * @summary org.omg.PortableServer.ThreadPolicyValue conformance: enumeration values.
 * @run main ThreadPolicyValueTest
 */

public class ThreadPolicyValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.ThreadPolicyValue.ORB_CTRL_MODEL != null, "ORB_CTRL_MODEL constant non-null");
        check(org.omg.PortableServer.ThreadPolicyValue.ORB_CTRL_MODEL.value() == org.omg.PortableServer.ThreadPolicyValue._ORB_CTRL_MODEL, "value() matches int constant");
        check(org.omg.PortableServer.ThreadPolicyValue.from_int(org.omg.PortableServer.ThreadPolicyValue._ORB_CTRL_MODEL) == org.omg.PortableServer.ThreadPolicyValue.ORB_CTRL_MODEL, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
