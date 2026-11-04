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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
