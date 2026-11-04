/*
 * @test
 * @summary org.omg.PortableServer.ImplicitActivationPolicyValue conformance: enumeration values.
 * @run main ImplicitActivationPolicyValueTest
 */

public class ImplicitActivationPolicyValueTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.PortableServer.ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION != null, "IMPLICIT_ACTIVATION constant non-null");
        check(org.omg.PortableServer.ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION.value() == org.omg.PortableServer.ImplicitActivationPolicyValue._IMPLICIT_ACTIVATION, "value() matches int constant");
        check(org.omg.PortableServer.ImplicitActivationPolicyValue.from_int(org.omg.PortableServer.ImplicitActivationPolicyValue._IMPLICIT_ACTIVATION) == org.omg.PortableServer.ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION, "from_int round-trips");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
