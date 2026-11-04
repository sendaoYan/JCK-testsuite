/*
 * @test
 * @summary org.omg.PortableServer.RequestProcessingPolicyValue conformance: enumeration values.
 * @run main RequestProcessingPolicyValueTest
 */

public class RequestProcessingPolicyValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY != null, "USE_ACTIVE_OBJECT_MAP_ONLY constant non-null");
        check(org.omg.PortableServer.RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY.value() == org.omg.PortableServer.RequestProcessingPolicyValue._USE_ACTIVE_OBJECT_MAP_ONLY, "value() matches int constant");
        check(org.omg.PortableServer.RequestProcessingPolicyValue.from_int(org.omg.PortableServer.RequestProcessingPolicyValue._USE_ACTIVE_OBJECT_MAP_ONLY) == org.omg.PortableServer.RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
