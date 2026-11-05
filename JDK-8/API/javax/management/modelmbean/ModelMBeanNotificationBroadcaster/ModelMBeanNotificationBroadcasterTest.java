/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanNotificationBroadcaster conformance: interface reflection.
 * @run main ModelMBeanNotificationBroadcasterTest
 */

import java.lang.reflect.Method;
import java.util.Set;
import java.util.HashSet;

public class ModelMBeanNotificationBroadcasterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanNotificationBroadcaster");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.NotificationBroadcaster").isAssignableFrom(c), "implements/extends javax.management.NotificationBroadcaster");
        check(hasMethod(c, "addAttributeChangeNotificationListener"), "declares addAttributeChangeNotificationListener");
        check(hasMethod(c, "removeAttributeChangeNotificationListener"), "declares removeAttributeChangeNotificationListener");
        check(hasMethod(c, "sendAttributeChangeNotification"), "declares sendAttributeChangeNotification");
        testDeepMethodSet();
        testDeepThroughRequiredModelMBean();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch h): exact method set, RequiredModelMBean as real implementation ----

    static void testDeepMethodSet() throws Exception {
        Class<?> c = javax.management.modelmbean.ModelMBeanNotificationBroadcaster.class;
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.size() == 7, "MMBNB exposes exactly 7 distinct method names (4 own + 3 inherited): " + names.size());
        check(names.contains("sendNotification") && names.contains("sendAttributeChangeNotification")
                && names.contains("addAttributeChangeNotificationListener")
                && names.contains("removeAttributeChangeNotificationListener"),
                "attribute-change broadcaster methods declared");
        check(names.contains("addNotificationListener") && names.contains("removeNotificationListener")
                && names.contains("getNotificationInfo"), "NotificationBroadcaster methods inherited");
        // sendNotification/sendAttributeChangeNotification each have two overloads
        int sendOverloads = 0;
        int acnOverloads = 0;
        for (java.lang.reflect.Method m : c.getMethods()) {
            if ("sendNotification".equals(m.getName())) sendOverloads++;
            if ("sendAttributeChangeNotification".equals(m.getName())) acnOverloads++;
        }
        check(sendOverloads == 2, "sendNotification has Notification and String overloads");
        check(acnOverloads == 2, "sendAttributeChangeNotification has ACN and (Attribute,Attribute) overloads");
        // addAttributeChangeNotificationListener declares IllegalArgumentException
        java.lang.reflect.Method add = c.getMethod("addAttributeChangeNotificationListener",
                javax.management.NotificationListener.class, String.class, Object.class);
        boolean iae = false;
        for (Class<?> ex : add.getExceptionTypes()) if (ex == IllegalArgumentException.class) iae = true;
        check(iae, "addAttributeChangeNotificationListener declares IllegalArgumentException");
    }

    static void testDeepThroughRequiredModelMBean() throws Exception {
        javax.management.modelmbean.RequiredModelMBean rmm = new javax.management.modelmbean.RequiredModelMBean();
        javax.management.modelmbean.ModelMBeanNotificationBroadcaster bc = rmm;
        final java.util.List<javax.management.Notification> got =
                java.util.Collections.synchronizedList(new java.util.ArrayList<javax.management.Notification>());
        bc.addNotificationListener(new javax.management.NotificationListener() {
            public void handleNotification(javax.management.Notification n, Object h) { got.add(n); }
        }, null, "hb");
        // sendNotification(Notification) delivers the same instance
        javax.management.Notification n = new javax.management.Notification("jck.bc", rmm, 3L);
        bc.sendNotification(n);
        check(got.size() == 1 && got.get(0) == n, "sendNotification delivers the same instance");
        // sendNotification(String) builds a jmx.modelmbean.generic notification
        bc.sendNotification("t");
        check(got.size() == 2 && "jmx.modelmbean.generic".equals(got.get(1).getType()),
                "sendNotification(String) builds the generic notification");
        // two-Attribute overload builds an AttributeChangeNotification
        got.clear();
        bc.sendAttributeChangeNotification(new javax.management.Attribute("A", 1), new javax.management.Attribute("A", 2));
        check(got.size() == 1 && got.get(0) instanceof javax.management.AttributeChangeNotification,
                "sendAttributeChangeNotification(Attribute,Attribute) builds an ACN");
        javax.management.AttributeChangeNotification acn = (javax.management.AttributeChangeNotification) got.get(0);
        check("A".equals(acn.getAttributeName()) && Integer.valueOf(1).equals(acn.getOldValue())
                && Integer.valueOf(2).equals(acn.getNewValue()), "built ACN carries old/new values");
        // attribute-change listener registration and removal (the attribute must exist in the info)
        javax.management.modelmbean.ModelMBeanInfo info = new javax.management.modelmbean.ModelMBeanInfoSupport(
                "C", "d",
                new javax.management.modelmbean.ModelMBeanAttributeInfo[]{
                    new javax.management.modelmbean.ModelMBeanAttributeInfo("A", "int", "a", true, true, false)},
                null, null, null);
        rmm.setModelMBeanInfo(info);
        javax.management.NotificationListener acnl = new javax.management.NotificationListener() {
            public void handleNotification(javax.management.Notification nn, Object h) { }
        };
        try {
            new javax.management.modelmbean.RequiredModelMBean()
                    .addAttributeChangeNotificationListener(acnl, "A", null);
            check(false, "ACN listener for unknown attribute must throw");
        } catch (javax.management.RuntimeOperationsException roe) {
            check(roe.getCause() instanceof IllegalArgumentException,
                    "ACN listener for unknown attribute -> RuntimeOperationsException wrapping IAE");
        }
        bc.addAttributeChangeNotificationListener(acnl, "A", null);
        try {
            bc.removeAttributeChangeNotificationListener(acnl, "A");
            check(true, "attribute-change listener removed");
        } catch (javax.management.ListenerNotFoundException lnfe) {
            check(false, "removing a registered attribute-change listener must succeed");
        }
        // removing an unregistered one throws ListenerNotFoundException
        try {
            bc.removeAttributeChangeNotificationListener(acnl, "A");
            check(false, "removing twice must throw");
        } catch (javax.management.ListenerNotFoundException lnfe) {
            check(true, "second removal -> ListenerNotFoundException");
        }
        // default notification info served through the broadcaster view
        check(bc.getNotificationInfo().length == 2, "default notification info: GENERIC + ATTRIBUTE_CHANGE");
    }
}
