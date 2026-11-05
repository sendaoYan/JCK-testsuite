/*
 * @test
 * @summary org.omg.CORBA.Object conformance: interface shape.
 * @run main ObjectTest
 */

public class ObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.Object");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(c.getInterfaces().length == 0, "extends no other Java interface");

        // ---- exact method-name surface (13 ops, javap-verified on jdk8u372) ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        java.util.Set<String> expected = new java.util.HashSet<String>(java.util.Arrays.asList(
            "_create_request", "_duplicate", "_get_domain_managers", "_get_interface_def",
            "_get_policy", "_hash", "_is_a", "_is_equivalent", "_non_existent", "_release",
            "_request", "_set_policy_override"));
        check(names.equals(expected), "Object declares exactly the 12 CORBA object ops: " + names);
        check(countByName(c, "_create_request") == 2, "_create_request has 2 overloads (4-arg and 6-arg)");
        check(countByName(c, "_request") == 1, "_request(String) single overload");

        // param shapes of key ops
        check(takes(c, "_is_a", String.class), "_is_a takes String repository id");
        check(takes(c, "_hash", int.class), "_hash takes int max");
        check(takes(c, "_get_policy", int.class), "_get_policy takes int policy type");
        check(takes(c, "_set_policy_override", org.omg.CORBA.Policy[].class),
              "_set_policy_override takes Policy[]");
        check(takes(c, "_set_policy_override", org.omg.CORBA.SetOverrideType.class),
              "_set_policy_override takes SetOverrideType");
        check(returns(c, "_non_existent", boolean.class), "_non_existent returns boolean");
        check(returns(c, "_duplicate", c), "_duplicate returns org.omg.CORBA.Object");

        // implementors available offline
        check(c.isAssignableFrom(org.omg.CORBA.LocalObject.class), "LocalObject implements Object");
        check(c.isAssignableFrom(org.omg.CORBA.portable.ObjectImpl.class), "ObjectImpl implements Object");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static int countByName(Class<?> c, String n) {
        int k = 0;
        for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(n)) k++;
        return k;
    }

    static boolean takes(Class<?> c, String n, Class<?> pt) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(n))
                for (Class<?> p : m.getParameterTypes()) if (p == pt) return true;
        return false;
    }

    static boolean returns(Class<?> c, String n, Class<?> rt) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(n) && m.getReturnType() == rt) return true;
        return false;
    }
}
