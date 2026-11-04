/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanServerDelegateMBean conformance: interface reflection.
 * @run main MBeanServerDelegateMBeanTest
 */

public class MBeanServerDelegateMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanServerDelegateMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getImplementationName"), "declares getImplementationName");
        check(hasMethod(c, "getImplementationVendor"), "declares getImplementationVendor");
        check(hasMethod(c, "getImplementationVersion"), "declares getImplementationVersion");
        deepInterfaceShape();
        deepThroughServerAndProxy();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== appended deepening (2026-11): shared helpers ====

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ==== appended deepening (2026-11): interface shape ====

    static void deepInterfaceShape() throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanServerDelegateMBean");
        check(c.isInterface(), "is an interface");
        java.util.Set<String> names = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        java.util.Set<String> expected = new java.util.TreeSet<String>(java.util.Arrays.asList(
                "getMBeanServerId", "getSpecificationName", "getSpecificationVersion", "getSpecificationVendor",
                "getImplementationName", "getImplementationVersion", "getImplementationVendor"));
        check(names.equals(expected), "exactly seven getters, got " + names);
        for (java.lang.reflect.Method m : c.getMethods()) {
            check(m.getParameterTypes().length == 0, m.getName() + " is parameterless");
            check(m.getReturnType() == String.class, m.getName() + " returns String");
            check(m.getExceptionTypes().length == 0, m.getName() + " declares no checked exceptions");
        }
        check(c.isAssignableFrom(javax.management.MBeanServerDelegate.class),
                "MBeanServerDelegate implements the interface");
    }

    // ==== appended deepening (2026-11): through interface reference, server, and proxy ====

    static void deepThroughServerAndProxy() throws Exception {
        javax.management.MBeanServerDelegate d = new javax.management.MBeanServerDelegate();
        javax.management.MBeanServerDelegateMBean m = d;
        check(m.getMBeanServerId().length() > 0, "getMBeanServerId through interface reference");
        check("Java Management Extensions".equals(m.getSpecificationName()), "getSpecificationName through interface");
        check("1.4".equals(m.getSpecificationVersion()), "getSpecificationVersion through interface");
        check(m.getSpecificationVendor() != null, "getSpecificationVendor through interface");
        check("JMX".equals(m.getImplementationName()), "getImplementationName through interface");
        check(m.getImplementationVersion() != null, "getImplementationVersion through interface");
        check(m.getImplementationVendor() != null, "getImplementationVendor through interface");
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName on = new javax.management.ObjectName("jck.dmb:type=Delegate");
        server.registerMBean(d, on);
        String[] attrs = {"MBeanServerId", "SpecificationName", "SpecificationVersion", "SpecificationVendor",
                "ImplementationName", "ImplementationVersion", "ImplementationVendor"};
        for (String attr : attrs) {
            check(server.getAttribute(on, attr) != null, "attribute " + attr + " non-null via server");
        }
        // introspected attribute names mirror the interface exactly
        java.util.Set<String> info = new java.util.HashSet<String>();
        for (javax.management.MBeanAttributeInfo a : server.getMBeanInfo(on).getAttributes()) info.add(a.getName());
        java.util.Set<String> exp = new java.util.HashSet<String>(java.util.Arrays.asList(attrs));
        check(info.equals(exp), "MBeanInfo attribute names equal the interface getters");
        // proxy closure: an MBeanServerInvocationHandler proxy typed as this interface
        javax.management.MBeanServerDelegateMBean proxy =
                javax.management.MBeanServerInvocationHandler.newProxyInstance(
                        server, on, javax.management.MBeanServerDelegateMBean.class, false);
        check("Java Management Extensions".equals(proxy.getSpecificationName()),
                "proxy getSpecificationName routes through the server");
        check(proxy.getMBeanServerId().equals(d.getMBeanServerId()), "proxy id equals the implementation id");
    }
}

