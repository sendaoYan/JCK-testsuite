/*
 * @test
 * @summary org.omg.PortableServer.ServantRetentionPolicy conformance: interface shape.
 * @run main ServantRetentionPolicyTest
 */

public class ServantRetentionPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.ServantRetentionPolicy");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.PortableServer.ServantRetentionPolicyOperations.class.isAssignableFrom(c), "extends org.omg.PortableServer.ServantRetentionPolicyOperations");
        check(org.omg.CORBA.Policy.class.isAssignableFrom(c), "extends org.omg.CORBA.Policy");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): no own methods / exact extends set / proxy routing ----
        check(c.getDeclaredMethods().length == 0,
              "policy interface declares no own methods (value() lives on org.omg.PortableServer.ServantRetentionPolicyOperations)");
        java.lang.reflect.Method vm = c.getMethod("value");
        check(vm.getReturnType() == org.omg.PortableServer.ServantRetentionPolicyValue.class, "inherited value() returns ServantRetentionPolicyValue");
        check(java.lang.reflect.Modifier.isAbstract(vm.getModifiers()), "value() is abstract");
        Class<?>[] ifaces = c.getInterfaces();
        check(ifaces.length == 3
           && ifaces[0] == org.omg.PortableServer.ServantRetentionPolicyOperations.class
           && ifaces[1] == org.omg.CORBA.Policy.class
           && ifaces[2] == org.omg.CORBA.portable.IDLEntity.class,
              "extends exactly { org.omg.PortableServer.ServantRetentionPolicyOperations, Policy, IDLEntity } in declaration order");
        check(org.omg.PortableServer.ServantRetentionPolicyOperations.class.getDeclaredMethods().length == 1,
              "org.omg.PortableServer.ServantRetentionPolicyOperations declares exactly one own method: value()");
        check(org.omg.PortableServer.ServantRetentionPolicyOperations.class.getInterfaces().length == 1
           && org.omg.PortableServer.ServantRetentionPolicyOperations.class.getInterfaces()[0] == org.omg.CORBA.PolicyOperations.class,
              "org.omg.PortableServer.ServantRetentionPolicyOperations parent is PolicyOperations only");
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
            if ("value".equals(m.getName())) return org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN;
            if ("policy_type".equals(m.getName())) { hitType[0] = true; return Integer.valueOf(21); }
            return null;
        };
        org.omg.PortableServer.ServantRetentionPolicy proxied = (org.omg.PortableServer.ServantRetentionPolicy) java.lang.reflect.Proxy.newProxyInstance(
            c.getClassLoader(), new Class<?>[]{c}, ih);
        check(proxied.value() == org.omg.PortableServer.ServantRetentionPolicyValue.RETAIN, "proxy value() routes RETAIN constant");
        check(proxied.policy_type() == 21 && hitType[0], "proxy policy_type() routes SERVANT_RETENTION_POLICY_ID == 21");
        check(proxied instanceof org.omg.CORBA.Policy, "proxy instance is a org.omg.CORBA.Policy");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
