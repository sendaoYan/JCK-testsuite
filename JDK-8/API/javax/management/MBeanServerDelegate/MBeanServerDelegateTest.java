/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanServerDelegate conformance: concrete class reflection.
 * @run main MBeanServerDelegateTest
 */

public class MBeanServerDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanServerDelegate");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("javax.management.MBeanServerDelegateMBean").isAssignableFrom(c), "implements/extends javax.management.MBeanServerDelegateMBean");
        check(Class.forName("javax.management.NotificationEmitter").isAssignableFrom(c), "implements/extends javax.management.NotificationEmitter");
        check(hasMethod(c, "addNotificationListener"), "declares addNotificationListener");
        check(hasMethod(c, "getImplementationName"), "declares getImplementationName");
        check(hasMethod(c, "getImplementationVendor"), "declares getImplementationVendor");
        deepDelegateOnServer();
        deepFreshDelegates();

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

    // ==== appended deepening (2026-11): delegate MBean on a local server ====

    static void deepDelegateOnServer() throws Exception {
        final javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        final javax.management.ObjectName dn = javax.management.MBeanServerDelegate.DELEGATE_NAME;
        check(server.isRegistered(dn), "delegate pre-registered under DELEGATE_NAME");
        check(server.getObjectInstance(dn).getClassName().endsWith("MBeanServerDelegate"),
                "delegate ObjectInstance className");
        check("Java Management Extensions".equals(server.getAttribute(dn, "SpecificationName")),
                "SpecificationName constant");
        check("1.4".equals(server.getAttribute(dn, "SpecificationVersion")),
                "SpecificationVersion 1.4 (both JDK 8 and JDK 27)");
        check(server.getAttribute(dn, "SpecificationVendor") != null, "SpecificationVendor non-null");
        check("JMX".equals(server.getAttribute(dn, "ImplementationName")), "ImplementationName is JMX");
        check(server.getAttribute(dn, "ImplementationVersion") != null, "ImplementationVersion non-null (JVM-specific)");
        check(server.getAttribute(dn, "ImplementationVendor") != null, "ImplementationVendor non-null");
        check(((String) server.getAttribute(dn, "MBeanServerId")).length() > 0, "MBeanServerId non-empty");
        javax.management.MBeanInfo info = server.getMBeanInfo(dn);
        java.util.Set<String> attrNames = new java.util.HashSet<String>();
        for (javax.management.MBeanAttributeInfo a : info.getAttributes()) {
            attrNames.add(a.getName());
            check(a.isReadable() && !a.isWritable(), "delegate attribute read-only: " + a.getName());
        }
        java.util.Set<String> expected = new java.util.HashSet<String>(java.util.Arrays.asList(
                "ImplementationName", "ImplementationVendor", "ImplementationVersion",
                "MBeanServerId", "SpecificationName", "SpecificationVendor", "SpecificationVersion"));
        check(attrNames.equals(expected), "delegate attribute set is exactly the seven constants");
        check(info.getOperations().length == 0, "delegate exposes no operations");
        check(info.getNotifications().length == 1, "delegate declares one notification info");
        check(info.getClassName().endsWith("MBeanServerDelegate"), "delegate MBeanInfo className");
        // delegate attributes are read-only: writes are rejected
        try {
            server.setAttribute(dn, new javax.management.Attribute("SpecificationName", "x"));
            check(false, "delegate write must be rejected");
        } catch (javax.management.AttributeNotFoundException e) {
            check(true, "write to read-only delegate attribute -> AttributeNotFoundException");
        }
    }

    // ==== appended deepening (2026-11): fresh delegate instances ====

    static void deepFreshDelegates() throws Exception {
        javax.management.MBeanServerDelegate d1 = new javax.management.MBeanServerDelegate();
        javax.management.MBeanServerDelegate d2 = new javax.management.MBeanServerDelegate();
        check(d1 instanceof javax.management.MBeanServerDelegateMBean, "implements MBeanServerDelegateMBean");
        check(d1 instanceof javax.management.NotificationEmitter, "implements NotificationEmitter");
        check(d1.getMBeanServerId().length() > 0, "id non-empty");
        check(!d1.getMBeanServerId().equals(d2.getMBeanServerId()), "fresh delegates get distinct ids");
        check("Java Management Extensions".equals(d1.getSpecificationName()), "getSpecificationName");
        check("1.4".equals(d1.getSpecificationVersion()), "getSpecificationVersion");
        check(d1.getSpecificationVendor() != null, "getSpecificationVendor non-null");
        check("JMX".equals(d1.getImplementationName()), "getImplementationName");
        check(d1.getImplementationVersion() != null, "getImplementationVersion non-null");
        check(d1.getImplementationVendor() != null, "getImplementationVendor non-null");
        // a delegate registered as an ordinary MBean under a custom name
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName on = new javax.management.ObjectName("jck.del:type=MyDelegate");
        javax.management.ObjectInstance oi = server.registerMBean(d1, on);
        check(on.equals(oi.getObjectName()), "custom-name registration");
        check(d1.getMBeanServerId().equals(server.getAttribute(on, "MBeanServerId")),
                "attribute routed to the registered delegate instance");
        check("Java Management Extensions".equals(server.getAttribute(on, "SpecificationName")),
                "SpecificationName via server");
        server.unregisterMBean(on);
    }
}

