/*
 * @test
 * @summary javax.management.StandardEmitterMBean conformance: white-box structure + black-box behavior.
 * @run main StandardEmitterMBeanTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.*;
import javax.management.*;

public class StandardEmitterMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    public interface ThingMBean {
        int getCount();
        void setCount(int c);
    }
    public static class Thing implements ThingMBean {
        private int count = 7;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.StandardEmitterMBean");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.getSuperclass() == StandardMBean.class, "extends StandardMBean");
        check(NotificationEmitter.class.isAssignableFrom(c), "implements NotificationEmitter");
        check(hasMethod(c, "addNotificationListener"), "has addNotificationListener");
        check(hasMethod(c, "removeNotificationListener"), "has removeNotificationListener");
        check(hasMethod(c, "getNotificationInfo"), "has getNotificationInfo");
        check(hasMethod(c, "sendNotification"), "has sendNotification");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        NotificationBroadcasterSupport emitter = new NotificationBroadcasterSupport(
                new MBeanNotificationInfo(new String[]{"test.type"}, "javax.management.Notification", "test"));
        StandardEmitterMBean mb = new StandardEmitterMBean(new Thing(), ThingMBean.class, emitter);

        // still a standard MBean
        check(mb.getMBeanInterface() == ThingMBean.class, "getMBeanInterface");
        check(mb.getMBeanInfo().getAttributes().length == 1, "attribute introspection");
        mb.setAttribute(new Attribute("Count", 11));
        check(mb.getAttribute("Count").equals(11), "attribute round-trip");

        // notification info exposed
        check(mb.getNotificationInfo().length == 1, "getNotificationInfo length");
        check("test.type".equals(mb.getNotificationInfo()[0].getNotifTypes()[0]), "notification info type");

        // send + receive through the emitter
        final List<Notification> received = new ArrayList<Notification>();
        NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification n, Object handback) { received.add(n); }
        };
        mb.addNotificationListener(listener, null, null);
        Notification n = new Notification("test.type", "src", 1L);
        mb.sendNotification(n);
        check(received.size() == 1, "listener received notification");
        check(received.get(0).getType().equals("test.type"), "received type");

        mb.removeNotificationListener(listener);
        mb.sendNotification(new Notification("test.type", "src", 2L));
        check(received.size() == 1, "removed listener receives nothing");

        // register against platform MBeanServer
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("jck.test:type=StandardEmitterMBeanTest");
        mbs.registerMBean(mb, on);
        try {
            check(mbs.isRegistered(on), "registered");
            check(mbs.getAttribute(on, "Count").equals(11), "MBeanServer attribute");
        } finally {
            mbs.unregisterMBean(on);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
