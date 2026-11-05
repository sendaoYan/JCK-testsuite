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
        testEmitterInfoMerge();
        testEmitterSendLoop();
        testEmitterFilterBlocks();
        testEmitterListenerRemoval();
        System.out.println("PASS (" + passed + " assertions)");
    }

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

    // ---- deepened (batch e): emitter wiring, notification round-trip through
    //      a registered server, filter/handback, listener removal precision ----

    public static interface ECounterMBean {
        int getCount(); void setCount(int v); void reset(); int add(int x); boolean isReady();
    }

    public static class ECounter implements ECounterMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int v) { count = v; }
        public void reset() { count = 0; }
        public int add(int x) { count += x; return count; }
        public boolean isReady() { return true; }
    }

    static final class CollectingListener implements NotificationListener {
        final List<Notification> seen = Collections.synchronizedList(new ArrayList<Notification>());
        final List<Object> handbacks = Collections.synchronizedList(new ArrayList<Object>());
        public void handleNotification(Notification n, Object h) { seen.add(n); handbacks.add(h); }
    }

    static void awaitSize(List<?> list, int expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (list.size() < expected && System.currentTimeMillis() < deadline) Thread.sleep(20);
    }

    static void testEmitterInfoMerge() throws Exception {
        MBeanNotificationInfo ninfo = new MBeanNotificationInfo(new String[]{"jck.evt"}, "jck.Evt", "emitter events");
        NotificationBroadcasterSupport emitter = new NotificationBroadcasterSupport(ninfo);
        StandardEmitterMBean mbean = new StandardEmitterMBean(new ECounter(), ECounterMBean.class, emitter);
        MBeanNotificationInfo[] infos = mbean.getNotificationInfo();
        check(infos.length == 1 && infos[0] == ninfo, "emitter notification info propagated (same instance)");
        MBeanNotificationInfo[] viaInfo = mbean.getMBeanInfo().getNotifications();
        check(viaInfo.length == 1 && viaInfo[0] == ninfo, "MBeanInfo notifications taken from emitter");
        check(mbean.getMBeanInfo().getAttributes().length == 2, "emitter MBeanInfo keeps {Count,Ready} attributes");
        StandardEmitterMBean fourArg = new StandardEmitterMBean(new ECounter(), ECounterMBean.class, false, emitter);
        check(fourArg.getMBeanInfo().getAttributes().length == 2, "4-arg ctor introspects same attributes");
        check(fourArg.getNotificationInfo().length == 1, "4-arg ctor keeps notification info");
    }

    static void testEmitterSendLoop() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        NotificationBroadcasterSupport emitter = new NotificationBroadcasterSupport();
        StandardEmitterMBean mbean = new StandardEmitterMBean(new ECounter(), ECounterMBean.class, emitter);
        ObjectName on = new ObjectName("jck.em", "k", "e");
        server.registerMBean(mbean, on);
        CollectingListener listener = new CollectingListener();
        Object handback = new Object();
        server.addNotificationListener(on, listener, null, handback);
        Notification sent = new Notification("jck.evt", on, 1L, "hello");
        mbean.sendNotification(sent);
        awaitSize(listener.seen, 1);
        check(listener.seen.size() == 1 && listener.seen.get(0) == sent,
                "listener received the very same notification instance");
        check(listener.handbacks.size() == 1 && listener.handbacks.get(0) == handback,
                "handback is the same instance");
        check(((Integer) server.getAttribute(on, "Count")).intValue() == 0, "emitter mbean attribute routing Count");
        check(Boolean.TRUE.equals(server.getAttribute(on, "Ready")), "emitter mbean attribute routing isIs Ready");
        check(((Integer) server.invoke(on, "add", new Object[]{Integer.valueOf(4)}, new String[]{"int"})).intValue() == 4,
                "emitter mbean invoke routing");
    }

    static void testEmitterFilterBlocks() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        NotificationBroadcasterSupport emitter = new NotificationBroadcasterSupport();
        StandardEmitterMBean mbean = new StandardEmitterMBean(new ECounter(), ECounterMBean.class, emitter);
        ObjectName on = new ObjectName("jck.em", "k", "f");
        server.registerMBean(mbean, on);
        final CollectingListener listener = new CollectingListener();
        NotificationFilter none = new NotificationFilter() {
            public boolean isNotificationEnabled(Notification n) { return false; }
        };
        server.addNotificationListener(on, listener, none, null);
        mbean.sendNotification(new Notification("jck.evt", on, 2L));
        Thread.sleep(150);
        check(listener.seen.isEmpty(), "filter returning false blocks delivery");
        server.removeNotificationListener(on, listener, none, null);
        NotificationFilter all = new NotificationFilter() {
            public boolean isNotificationEnabled(Notification n) { return true; }
        };
        server.addNotificationListener(on, listener, all, null);
        Notification second = new Notification("jck.evt", on, 3L);
        mbean.sendNotification(second);
        awaitSize(listener.seen, 1);
        check(listener.seen.size() == 1 && listener.seen.get(0) == second, "accepting filter delivers");
    }

    static void testEmitterListenerRemoval() throws Exception {
        final MBeanServer server = MBeanServerFactory.newMBeanServer();
        NotificationBroadcasterSupport emitter = new NotificationBroadcasterSupport();
        final StandardEmitterMBean mbean = new StandardEmitterMBean(new ECounter(), ECounterMBean.class, emitter);
        final NotificationListener l1 = new CollectingListener();
        final NotificationFilter f1 = new NotificationFilter() {
            public boolean isNotificationEnabled(Notification n) { return true; }
        };
        final NotificationFilter f2 = new NotificationFilter() {
            public boolean isNotificationEnabled(Notification n) { return false; }
        };
        mbean.addNotificationListener(l1, f1, "hb");
        checkThrows(ListenerNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { mbean.removeNotificationListener(l1, f1, "other"); }
        }, "remove with wrong handback fails");
        checkThrows(ListenerNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { mbean.removeNotificationListener(l1, f2, "hb"); }
        }, "remove with wrong filter fails");
        checkThrows(ListenerNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { mbean.removeNotificationListener(new CollectingListener(), f1, "hb"); }
        }, "remove with unregistered listener fails");
        mbean.removeNotificationListener(l1, f1, "hb");
        check(true, "exact (listener,filter,handback) removal succeeds");
        checkThrows(ListenerNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { mbean.removeNotificationListener(l1, f1, "hb"); }
        }, "removing twice fails");
        checkThrows(ListenerNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { mbean.removeNotificationListener(l1); }
        }, "1-arg remove of unregistered listener fails");
        checkThrows(RuntimeOperationsException.class, new ThrowingRunnable() {
            public void run() throws Exception {
                server.registerMBean(mbean, new ObjectName("jck.em", "k", "r"));
                server.addNotificationListener(new ObjectName("jck.em", "k", "r"), (NotificationListener) null, null, null);
            }
        }, "server rejects null listener");
    }
}
