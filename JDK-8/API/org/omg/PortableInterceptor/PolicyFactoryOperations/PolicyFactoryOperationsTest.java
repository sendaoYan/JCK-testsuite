/*
 * @test
 * @summary org.omg.PortableInterceptor.PolicyFactoryOperations conformance: interface shape.
 * @run main PolicyFactoryOperationsTest
 */

public class PolicyFactoryOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.PolicyFactoryOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (jdk8u372-b07 probed): single factory method ----
        java.util.Set<String> policyfactoryoperations_m = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.PolicyFactoryOperations.class.getDeclaredMethods()) policyfactoryoperations_m.add(m.getName());
        check(policyfactoryoperations_m.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("create_policy"))), "method-name set exact (1 methods)");
        check(org.omg.PortableInterceptor.PolicyFactoryOperations.class.getDeclaredMethods().length == 1, "declared method count == 1");
        java.lang.reflect.Method cp = org.omg.PortableInterceptor.PolicyFactoryOperations.class.getDeclaredMethod(
            "create_policy", int.class, org.omg.CORBA.Any.class);
        check(cp.getReturnType() == org.omg.CORBA.Policy.class, "returns org.omg.CORBA.Policy");
        check(cp.getExceptionTypes()[0] == org.omg.CORBA.PolicyError.class, "declares org.omg.CORBA.PolicyError");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
