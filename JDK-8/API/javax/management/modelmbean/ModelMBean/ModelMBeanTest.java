/*
 * @test
 * @summary javax.management.modelmbean.ModelMBean conformance: interface reflection.
 * @run main ModelMBeanTest
 */

import java.lang.reflect.Method;

public class ModelMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.DynamicMBean").isAssignableFrom(c), "implements/extends javax.management.DynamicMBean");
        check(Class.forName("javax.management.PersistentMBean").isAssignableFrom(c), "implements/extends javax.management.PersistentMBean");
        check(Class.forName("javax.management.modelmbean.ModelMBeanNotificationBroadcaster").isAssignableFrom(c), "implements/extends javax.management.modelmbean.ModelMBeanNotificationBroadcaster");
        check(hasMethod(c, "setManagedResource"), "declares setManagedResource");
        check(hasMethod(c, "setModelMBeanInfo"), "declares setModelMBeanInfo");
        testDeepMethodSet();
        testDeepPolymorphicUse();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch h): full method set, polymorphic server use ----

    static java.util.Set<String> methodNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Method m : c.getMethods()) s.add(m.getName());
        return s;
    }

    static void testDeepMethodSet() {
        java.util.Set<String> names = methodNames(javax.management.modelmbean.ModelMBean.class);
        check(names.size() == 17, "ModelMBean exposes exactly 17 distinct method names (2 own + inherited): " + names.size());
        check(names.contains("setModelMBeanInfo") && names.contains("setManagedResource"),
                "own methods declared");
        // inherited from DynamicMBean
        for (String n : new String[]{"getMBeanInfo", "getAttribute", "getAttributes", "setAttribute",
                "setAttributes", "invoke"})
            check(names.contains(n), "DynamicMBean method " + n);
        // inherited from PersistentMBean
        check(names.contains("load") && names.contains("store"), "PersistentMBean methods");
        // inherited from ModelMBeanNotificationBroadcaster / NotificationBroadcaster
        for (String n : new String[]{"sendNotification", "sendAttributeChangeNotification",
                "addAttributeChangeNotificationListener", "removeAttributeChangeNotificationListener",
                "addNotificationListener", "removeNotificationListener", "getNotificationInfo"})
            check(names.contains(n), "broadcaster method " + n);
        // setManagedResource declares the checked InvalidTargetObjectTypeException
        try {
            javax.management.modelmbean.ModelMBean.class.getMethod("setManagedResource", Object.class, String.class);
            check(true, "setManagedResource signature resolvable");
        } catch (Exception e) {
            check(false, "setManagedResource signature resolvable");
        }
    }

    static void testDeepPolymorphicUse() throws Exception {
        // one RequiredModelMBean used through all four supertype references
        javax.management.modelmbean.RequiredModelMBean impl = new javax.management.modelmbean.RequiredModelMBean();
        javax.management.modelmbean.ModelMBean asModelMBean = impl;
        javax.management.DynamicMBean asDynamic = impl;
        javax.management.PersistentMBean asPersistent = impl;
        javax.management.modelmbean.ModelMBeanNotificationBroadcaster asBroadcaster = impl;
        check(asModelMBean != null && asDynamic != null && asPersistent != null && asBroadcaster != null,
                "all supertype views available");
        javax.management.modelmbean.ModelMBeanInfo info = new javax.management.modelmbean.ModelMBeanInfoSupport(
                "Poly", "d",
                new javax.management.modelmbean.ModelMBeanAttributeInfo[0],
                new javax.management.modelmbean.ModelMBeanConstructorInfo[0],
                new javax.management.modelmbean.ModelMBeanOperationInfo[0],
                new javax.management.modelmbean.ModelMBeanNotificationInfo[0]);
        asModelMBean.setModelMBeanInfo(info);
        asModelMBean.setManagedResource(new Object(), "ObjectReference");
        check(asDynamic.getMBeanInfo().getClassName().equals("Poly"), "DynamicMBean view serves the info");
        try {
            asDynamic.getAttribute("Any");
            check(false, "getAttribute on empty info must throw");
        } catch (javax.management.AttributeNotFoundException anfe) {
            check(true, "DynamicMBean view getAttribute -> AttributeNotFoundException");
        }
        try {
            asPersistent.load();
            check(false, "PersistentMBean.load must throw");
        } catch (javax.management.MBeanException me) {
            check(true, "PersistentMBean.load -> MBeanException (persistence unsupported)");
        }
        // broadcaster view delivers notifications
        final java.util.List<javax.management.Notification> got =
                java.util.Collections.synchronizedList(new java.util.ArrayList<javax.management.Notification>());
        asBroadcaster.addNotificationListener(new javax.management.NotificationListener() {
            public void handleNotification(javax.management.Notification n, Object h) { got.add(n); }
        }, null, null);
        javax.management.Notification sent = new javax.management.Notification("jck.mm", impl, 1L);
        asBroadcaster.sendNotification(sent);
        check(got.size() == 1 && got.get(0) == sent, "broadcaster view delivers sendNotification");
        // registered through the MBeanServer as a DynamicMBean
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName on = new javax.management.ObjectName("jck.mmbdeep:type=Poly");
        server.registerMBean(asDynamic, on);
        check(server.isRegistered(on), "ModelMBean registered through the interface reference");
        server.unregisterMBean(on);
    }
}
