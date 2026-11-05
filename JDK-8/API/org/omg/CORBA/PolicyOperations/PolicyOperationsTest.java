/*
 * @test
 * @summary org.omg.CORBA.PolicyOperations conformance: interface shape.
 * @run main PolicyOperationsTest
 */

public class PolicyOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.PolicyOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");

        // ---- deepened: exact method surface (javap-verified on jdk8u372) ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CORBA.PolicyOperations.class.getMethods()) names.add(m.getName());
        java.util.Set<String> expected = new java.util.HashSet<String>(java.util.Arrays.asList("copy","destroy","policy_type"));
        check(names.equals(expected), "PolicyOperations declares exactly copy,destroy,policy_type: " + names);


        try {
            check(org.omg.CORBA.PolicyOperations.class.getMethod("policy_type").getReturnType() == int.class,
                  "policy_type() returns int");
            check(org.omg.CORBA.PolicyOperations.class.getMethod("copy").getReturnType() == org.omg.CORBA.Policy.class,
                  "copy() returns Policy");
            check(org.omg.CORBA.PolicyOperations.class.getMethod("destroy").getReturnType() == void.class,
                  "destroy() returns void");
        } catch (Exception e) { throw new AssertionError(e); }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
