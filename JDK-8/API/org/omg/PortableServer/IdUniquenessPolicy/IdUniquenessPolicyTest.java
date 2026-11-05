/*
 * @test
 * @summary org.omg.PortableServer.IdUniquenessPolicy conformance: interface shape.
 * @run main IdUniquenessPolicyTest
 */

public class IdUniquenessPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.IdUniquenessPolicy");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.IdUniquenessPolicyOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.IdUniquenessPolicyOperations");
        check(org.omg.CORBA.Policy.class.isAssignableFrom(c), "extends org.omg.CORBA.Policy");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): no own methods / exact extends set / proxy routing ----
        check(c.getDeclaredMethods().length == 0,
              "policy interface declares no own methods (value() lives on org.omg.PortableServer.IdUniquenessPolicyOperations)");
        java.lang.reflect.Method vm = c.getMethod("value");
        check(vm.getReturnType() == org.omg.PortableServer.IdUniquenessPolicyValue.class, "inherited value() returns IdUniquenessPolicyValue");
        check(java.lang.reflect.Modifier.isAbstract(vm.getModifiers()), "value() is abstract");
        Class<?>[] ifaces = c.getInterfaces();
        check(ifaces.length == 3
           && ifaces[0] == org.omg.PortableServer.IdUniquenessPolicyOperations.class
           && ifaces[1] == org.omg.CORBA.Policy.class
           && ifaces[2] == org.omg.CORBA.portable.IDLEntity.class,
              "extends exactly { org.omg.PortableServer.IdUniquenessPolicyOperations, Policy, IDLEntity } in declaration order");
        check(org.omg.PortableServer.IdUniquenessPolicyOperations.class.getDeclaredMethods().length == 1,
              "org.omg.PortableServer.IdUniquenessPolicyOperations declares exactly one own method: value()");
        check(org.omg.PortableServer.IdUniquenessPolicyOperations.class.getInterfaces().length == 1
           && org.omg.PortableServer.IdUniquenessPolicyOperations.class.getInterfaces()[0] == org.omg.CORBA.PolicyOperations.class,
              "org.omg.PortableServer.IdUniquenessPolicyOperations parent is PolicyOperations only");
        check(org.omg.CORBA.PolicyOperations.class.getDeclaredMethods().length == 3,
              "PolicyOperations base face = policy_type/copy/destroy (3 methods)");
        check(org.omg.CORBA.PolicyOperations.class.getMethod("policy_type").getReturnType() == int.class,
              "policy_type() returns int");
        check(org.omg.CORBA.PolicyOperations.class.getMethod("copy").getReturnType() == org.omg.CORBA.Policy.class,
              "copy() returns Policy");
        check(org.omg.CORBA.PolicyOperations.class.getMethod("destroy").getReturnType() == void.class,
              "destroy() returns void");
        final boolean[] hitType = new boolean[1];
        java.lang.reflect.InvocationHandler ih = (px, m, av) -> {
            if ("value".equals(m.getName())) return org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID;
            if ("policy_type".equals(m.getName())) { hitType[0] = true; return Integer.valueOf(18); }
            return null;
        };
        org.omg.PortableServer.IdUniquenessPolicy proxied = (org.omg.PortableServer.IdUniquenessPolicy) java.lang.reflect.Proxy.newProxyInstance(
            c.getClassLoader(), new Class<?>[]{c}, ih);
        check(proxied.value() == org.omg.PortableServer.IdUniquenessPolicyValue.UNIQUE_ID, "proxy value() routes UNIQUE_ID constant");
        check(proxied.policy_type() == 18 && hitType[0], "proxy policy_type() routes ID_UNIQUENESS_POLICY_ID == 18");
        check(proxied instanceof org.omg.CORBA.Policy, "proxy instance is a org.omg.CORBA.Policy");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
