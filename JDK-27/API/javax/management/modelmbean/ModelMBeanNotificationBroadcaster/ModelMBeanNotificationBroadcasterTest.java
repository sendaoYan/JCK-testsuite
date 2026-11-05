/*
 * @test
 * @summary javax.management.modelmbean.ModelMBeanNotificationBroadcaster conformance: white-box structure + black-box behavior.
 * @run main ModelMBeanNotificationBroadcasterTest
 */

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.modelmbean.ModelMBeanNotificationBroadcaster;

public class ModelMBeanNotificationBroadcasterTest {
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
        Class<?> c = Class.forName("javax.management.modelmbean.ModelMBeanNotificationBroadcaster");
        check(c.isInterface(), "is interface");
        check(NotificationBroadcaster.class.isAssignableFrom(c), "extends NotificationBroadcaster");
        Method[] mm = c.getMethods();
        check(has(mm, "sendNotification"), "has sendNotification");
        check(has(mm, "sendAttributeChangeNotification"), "has sendAttributeChangeNotification");
        check(has(mm, "addAttributeChangeNotificationListener"), "has addAttributeChangeNotificationListener");
        check(has(mm, "removeAttributeChangeNotificationListener"), "has removeAttributeChangeNotificationListener");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final AtomicInteger genericCount = new AtomicInteger();
        final AtomicInteger attrListenerAdds = new AtomicInteger();

        NotificationBroadcasterSupport delegate = new NotificationBroadcasterSupport();
        ModelMBeanNotificationBroadcaster mb = new ModelMBeanNotificationBroadcaster() {
            private final AtomicInteger seq = new AtomicInteger();
            public void addNotificationListener(NotificationListener l, NotificationFilter f, Object h) {
                delegate.addNotificationListener(l, f, h);
            }
            public void removeNotificationListener(NotificationListener l) throws javax.management.ListenerNotFoundException {
                delegate.removeNotificationListener(l);
            }
            public javax.management.MBeanNotificationInfo[] getNotificationInfo() {
                return new javax.management.MBeanNotificationInfo[0];
            }
            public void sendNotification(Notification n) {
                delegate.sendNotification(n);
            }
            public void sendNotification(String type) {
                genericCount.incrementAndGet();
                delegate.sendNotification(new Notification(type, this, seq.incrementAndGet()));
            }
            public void sendAttributeChangeNotification(AttributeChangeNotification n) {
                delegate.sendNotification(n);
            }
            public void sendAttributeChangeNotification(Attribute oldV, Attribute newV) {
                delegate.sendNotification(new AttributeChangeNotification(
                        this, seq.incrementAndGet(), System.currentTimeMillis(),
                        "attr change", newV.getName(), newV.getValue().getClass().getName(),
                        oldV.getValue(), newV.getValue()));
            }
            public void addAttributeChangeNotificationListener(
                    NotificationListener l, String name, Object h) {
                attrListenerAdds.incrementAndGet();
            }
            public void removeAttributeChangeNotificationListener(
                    NotificationListener l, String name) {
                attrListenerAdds.decrementAndGet();
            }
        };

        final AtomicInteger received = new AtomicInteger();
        NotificationListener sink = new NotificationListener() {
            public void handleNotification(Notification n, Object h) {
                received.incrementAndGet();
            }
        };
        mb.addNotificationListener(sink, null, null);

        mb.sendNotification("test.type");
        check(received.get() == 1, "sendNotification(String) delivered");
        check(genericCount.get() == 1, "sendNotification(String) invoked");

        mb.sendNotification(new Notification("test.other", mb, 99L));
        check(received.get() == 2, "sendNotification(Notification) delivered");

        // Attribute-change listener registration/unregistration are callable.
        NotificationListener l = new NotificationListener() {
            public void handleNotification(Notification n, Object h) {}
        };
        mb.addAttributeChangeNotificationListener(l, "Name", null);
        check(attrListenerAdds.get() == 1, "addAttributeChangeNotificationListener callable");
        mb.removeAttributeChangeNotificationListener(l, "Name");
        check(attrListenerAdds.get() == 0, "removeAttributeChangeNotificationListener callable");

        mb.removeNotificationListener(sink);
        // sendNotification with no listeners still completes without error.
        mb.sendNotification("test.no.listeners");
        check(received.get() == 2, "no new delivery after remove");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
