/*
 * @test
 * @summary org.omg.PortableServer.RequestProcessingPolicy conformance: interface shape.
 * @run main RequestProcessingPolicyTest
 */

public class RequestProcessingPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.RequestProcessingPolicy");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.RequestProcessingPolicyOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.RequestProcessingPolicyOperations");
        check(org.omg.CORBA.Policy.class.isAssignableFrom(c), "extends org.omg.CORBA.Policy");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
