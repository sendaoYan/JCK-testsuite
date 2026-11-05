/*
 * @test
 * @summary org.omg.CORBA.DomainManagerOperations conformance: interface shape.
 * @run main DomainManagerOperationsTest
 */

public class DomainManagerOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.DomainManagerOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");

        // ---- deepened: exact method surface (javap-verified on jdk8u372) ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CORBA.DomainManagerOperations.class.getMethods()) names.add(m.getName());
        java.util.Set<String> expected = new java.util.HashSet<String>(java.util.Arrays.asList("get_domain_policy"));
        check(names.equals(expected), "DomainManagerOperations declares exactly get_domain_policy: " + names);


        try {
            check(org.omg.CORBA.DomainManagerOperations.class.getMethod("get_domain_policy", int.class)
                    .getReturnType() == org.omg.CORBA.Policy.class, "get_domain_policy(int) returns Policy");
        } catch (Exception e) { throw new AssertionError(e); }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
