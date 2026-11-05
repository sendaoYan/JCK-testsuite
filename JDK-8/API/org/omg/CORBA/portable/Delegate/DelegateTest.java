/*
 * @test
 * @summary org.omg.CORBA.portable.Delegate conformance: abstract class shape.
 * @run main DelegateTest
 */

public class DelegateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.Delegate");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");

        // ---- deepened: exact method surface (javap-verified on jdk8u372) ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 21, "Delegate declares 21 uniquely-named methods, got " + names.size());
        check(c.getDeclaredMethods().length == 23,
              "23 declared methods counting the request/create_request overloads, got "
              + c.getDeclaredMethods().length);
        String[] must = {"get_interface_def", "duplicate", "release", "is_a", "non_existent", "is_equivalent",
                         "hash", "request", "create_request", "orb", "get_policy", "get_domain_managers",
                         "set_policy_override", "is_local", "servant_preinvoke", "servant_postinvoke",
                         "invoke", "releaseReply", "toString", "hashCode", "equals"};
        for (String n : must) check(names.contains(n), "Delegate declares " + n);
        check(java.util.Arrays.stream(c.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isAbstract(m.getModifiers())).count() == 10,
              "10 abstract core ops, rest are concrete helpers (toString/hashCode/equals/orb/is_local)");
        check(c.getMethod("orb", org.omg.CORBA.Object.class).getReturnType() == org.omg.CORBA.ORB.class,
              "orb(Object) concrete helper returns ORB");
        check(c.getMethod("is_local", org.omg.CORBA.Object.class).getReturnType() == boolean.class,
              "is_local concrete helper returns boolean");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
