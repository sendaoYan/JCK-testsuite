/*
 * @test
 * @summary javax.management.NotificationListener conformance: white-box structure + light behavior.
 * @run main NotificationListenerTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class NotificationListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.NotificationListener");
        check(c.isInterface(), "is interface");
        check(java.util.EventListener.class.isAssignableFrom(c), "extends EventListener");
        check(hasMethod(c, "handleNotification"), "has handleNotification");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final List<String> types = new ArrayList<String>();
        final List<Object> handbacks = new ArrayList<Object>();
        NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification n, Object handback) {
                types.add(n.getType());
                handbacks.add(handback);
            }
        };
        Notification n = new Notification("type", "src", 1L);
        listener.handleNotification(n, "hb");
        check(types.size() == 1 && "type".equals(types.get(0)), "handleNotification receives type");
        check("hb".equals(handbacks.get(0)), "handleNotification receives handback");
    }

    // ---- deepened (batch d): self-contained MBean registered on a fresh server ----
    public static interface CounterMBean {
        int getCount();
        void setCount(int v);
        void reset();
        int add(int x);
    }

    public static class Counter implements CounterMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int v) { count = v; }
        public void reset() { count = 0; }
        public int add(int x) { count += x; return count; }
    }

    static void testListenerShape() throws Exception {
        Class<?> c = load("javax.management.NotificationListener");
        check(c.getDeclaredMethods().length == 1, "exactly one declared method");
        Method h = c.getDeclaredMethod("handleNotification", Notification.class, Object.class);
        check(h.getReturnType() == void.class, "handleNotification returns void");
        check(h.getExceptionTypes().length == 0, "handleNotification declares no checked exceptions");
        // functional-interface use via lambda
        final List<String> got = new ArrayList<String>();
        NotificationListener lambda = (n, hb) -> { got.add(n.getType() + "/" + hb); };
        lambda.handleNotification(new Notification("lam", "s", 1L), "hb");
        check(got.contains("lam/hb"), "lambda listener receives type and handback");
    }

    static void testDelegateRegistrationLoop() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        final List<Notification> received = new ArrayList<Notification>();
        final List<Object> handbacks = new ArrayList<Object>();
        NotificationListener l = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { received.add(n); handbacks.add(h); }
        };
        Object handback = "delegate-hb";
        server.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, l, null, handback);

        ObjectName name = new ObjectName("jck.listener:type=Counter");
        server.registerMBean(new Counter(), name);
        // delegate notifications are synchronous, but poll defensively with a bounded loop
        for (long deadline = System.currentTimeMillis() + 5000L;
             received.isEmpty() && System.currentTimeMillis() < deadline; ) Thread.sleep(20L);
        check(received.size() == 1, "registration notification received from the delegate");
        Notification rn = received.get(0);
        check("JMX.mbean.registered".equals(rn.getType()),
                "registration type is JMX.mbean.registered");
        check(MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(rn.getType()),
                "matches MBeanServerNotification.REGISTRATION_NOTIFICATION");
        check(rn instanceof MBeanServerNotification, "delegate emits MBeanServerNotification");
        check(name.equals(((MBeanServerNotification) rn).getMBeanName()),
                "getMBeanName is the just-registered ObjectName");
        check("delegate-hb".equals(handbacks.get(0)), "handback delivered with the delegate notification");
        check(rn.getSource() != null, "delegate notification carries a source");

        server.unregisterMBean(name);
        for (long deadline = System.currentTimeMillis() + 5000L;
             received.size() < 2 && System.currentTimeMillis() < deadline; ) Thread.sleep(20L);
        check(received.size() == 2, "unregistration notification received from the delegate");
        check("JMX.mbean.unregistered".equals(received.get(1).getType()),
                "unregistration type is JMX.mbean.unregistered");
        check(MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(received.get(1).getType()),
                "matches MBeanServerNotification.UNREGISTRATION_NOTIFICATION");
        check(name.equals(((MBeanServerNotification) received.get(1)).getMBeanName()),
                "unregistration getMBeanName");

        server.removeNotificationListener(MBeanServerDelegate.DELEGATE_NAME, l);
        server.registerMBean(new Counter(), name);
        server.unregisterMBean(name);
        check(received.size() == 2, "listener detached from the delegate receives nothing");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testListenerShape();
        testDelegateRegistrationLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
