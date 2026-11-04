/*
 * @test
 * @summary org.omg.PortableServer.ServantRetentionPolicyOperations conformance: interface shape.
 * @run main ServantRetentionPolicyOperationsTest
 */

public class ServantRetentionPolicyOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.ServantRetentionPolicyOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.PolicyOperations.class.isAssignableFrom(c), "extends org.omg.CORBA.PolicyOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
