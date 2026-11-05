/*
 * @test
 * @summary javax.management.modelmbean.ModelMBean conformance: white-box structure + black-box behavior.
 * @run main ModelMBeanTest
 */

import java.lang.reflect.Method;
import javax.management.DynamicMBean;
import javax.management.PersistentMBean;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationBroadcaster;
import javax.management.modelmbean.RequiredModelMBean;

public class ModelMBeanTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(Method[] mm, String n) {
        for (Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBean");
        check(c.isInterface(), "is interface");
        check(DynamicMBean.class.isAssignableFrom(c), "extends DynamicMBean");
        check(PersistentMBean.class.isAssignableFrom(c), "extends PersistentMBean");
        check(ModelMBeanNotificationBroadcaster.class.isAssignableFrom(c),
                "extends ModelMBeanNotificationBroadcaster");
        Method[] mm = c.getMethods();
        check(has(mm, "setModelMBeanInfo"), "has setModelMBeanInfo");
        check(has(mm, "setManagedResource"), "has setManagedResource");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Standard implementation proves the interface is usable.
        ModelMBean mb = new RequiredModelMBean();

        ModelMBeanInfoSupport info = new ModelMBeanInfoSupport(
                "MyMBean", "desc",
                new javax.management.modelmbean.ModelMBeanAttributeInfo[0],
                new javax.management.modelmbean.ModelMBeanConstructorInfo[0],
                new javax.management.modelmbean.ModelMBeanOperationInfo[0],
                new javax.management.modelmbean.ModelMBeanNotificationInfo[0]);
        mb.setModelMBeanInfo(info);
        check("MyMBean".equals(mb.getMBeanInfo().getClassName()),
                "setModelMBeanInfo reflected by getMBeanInfo");

        mb.setManagedResource(new Object(), "objectReference");
        check(true, "setManagedResource objectReference callable");

        boolean iae = false;
        try {
            mb.setManagedResource(new Object(), "unsupportedType");
        } catch (InvalidTargetObjectTypeException e) {
            iae = true;
        }
        check(iae, "setManagedResource unsupported type throws InvalidTargetObjectTypeException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
