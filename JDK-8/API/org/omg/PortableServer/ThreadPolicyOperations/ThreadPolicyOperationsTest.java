/*
 * @test
 * @summary org.omg.PortableServer.ThreadPolicyOperations conformance: interface shape.
 * @run main ThreadPolicyOperationsTest
 */

public class ThreadPolicyOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.ThreadPolicyOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.PolicyOperations.class.isAssignableFrom(c), "extends org.omg.CORBA.PolicyOperations");
        // ---- deepened: value() method face exact + PolicyOperations-only parent ----
        java.lang.reflect.Method vm = c.getDeclaredMethod("value");
        check(vm.getReturnType() == org.omg.PortableServer.ThreadPolicyValue.class, "value() returns ThreadPolicyValue");
        check(java.lang.reflect.Modifier.isAbstract(vm.getModifiers()), "value() is abstract");
        check(c.getDeclaredMethods().length == 1, "exactly one own method: value()");
        check(c.getInterfaces().length == 1 && c.getInterfaces()[0] == org.omg.CORBA.PolicyOperations.class,
              "direct parent is org.omg.CORBA.PolicyOperations only");
        check(org.omg.CORBA.PolicyOperations.class.getDeclaredMethods().length == 3,
              "PolicyOperations base face = policy_type/copy/destroy");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
