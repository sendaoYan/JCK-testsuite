/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanServerNotification conformance: concrete class reflection.
 * @run main MBeanServerNotificationTest
 */

public class MBeanServerNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.MBeanServerNotification");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.Notification".equals(c.getSuperclass().getName()), "superclass javax.management.Notification");
        check(hasMethod(c, "getMBeanName"), "declares getMBeanName");
        check(hasMethod(c, "toString"), "declares toString");
        deepConstructorContract();
        deepRegisterUnregisterLoop();
        deepFilterBranch();

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

    // ==== appended deepening (2026-11): closed-loop type ====

    public static interface LedMBean { int getState(); }
    public static class Led implements LedMBean { public int getState() { return 1; } }

    // ==== appended deepening (2026-11): constructor contract ====

    static void deepConstructorContract() throws Exception {
        javax.management.ObjectName on = new javax.management.ObjectName("jck.notif:type=Led");
        javax.management.MBeanServerNotification reg = new javax.management.MBeanServerNotification(
                javax.management.MBeanServerNotification.REGISTRATION_NOTIFICATION, "src", 7L, on);
        check("JMX.mbean.registered".equals(javax.management.MBeanServerNotification.REGISTRATION_NOTIFICATION),
                "REGISTRATION_NOTIFICATION constant");
        check("JMX.mbean.unregistered".equals(javax.management.MBeanServerNotification.UNREGISTRATION_NOTIFICATION),
                "UNREGISTRATION_NOTIFICATION constant");
        check(javax.management.MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(reg.getType()),
                "getType is the registration constant");
        check(on.equals(reg.getMBeanName()), "getMBeanName exact");
        check(reg.getSequenceNumber() == 7L, "sequence number preserved");
        check("src".equals(reg.getSource()), "source preserved");
        check(reg.getMessage() == null || reg.getMessage().length() == 0, "message empty by default");
        check(reg.getTimeStamp() >= 0L, "time stamp non-negative");
        reg.setUserData("payload");
        check("payload".equals(reg.getUserData()), "setUserData round-trip");
        check(reg.toString().contains("JMX.mbean.registered") && reg.toString().contains("jck.notif:type=Led"),
                "toString carries type and mbean name");
        javax.management.MBeanServerNotification unreg = new javax.management.MBeanServerNotification(
                javax.management.MBeanServerNotification.UNREGISTRATION_NOTIFICATION, "src", 8L, on);
        check(javax.management.MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(unreg.getType()),
                "unregistration constructor type");
        check(!reg.getType().equals(unreg.getType()), "registration and unregistration types differ");
        javax.management.MBeanServerNotification nullName = new javax.management.MBeanServerNotification(
                javax.management.MBeanServerNotification.REGISTRATION_NOTIFICATION, "src", 9L, null);
        check(nullName.getMBeanName() == null, "null ObjectName preserved by the constructor");
    }

    // ==== appended deepening (2026-11): real register/unregister notification loop ====

    static void deepRegisterUnregisterLoop() throws Exception {
        final javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        final java.util.List<javax.management.MBeanServerNotification> got =
                new java.util.ArrayList<javax.management.MBeanServerNotification>();
        final java.util.List<Object> handbacks = new java.util.ArrayList<Object>();
        final Object handback = new Object();
        final javax.management.NotificationListener li = new javax.management.NotificationListener() {
            public void handleNotification(javax.management.Notification n, Object h) {
                if (n instanceof javax.management.MBeanServerNotification) {
                    got.add((javax.management.MBeanServerNotification) n);
                    handbacks.add(h);
                }
            }
        };
        server.addNotificationListener(javax.management.MBeanServerDelegate.DELEGATE_NAME, li, null, handback);
        final javax.management.ObjectName on = new javax.management.ObjectName("jck.notif:type=Ephemeral");
        server.registerMBean(new Led(), on);
        server.unregisterMBean(on);
        long deadline = System.currentTimeMillis() + 5000L;
        while (got.size() < 2 && System.currentTimeMillis() < deadline) Thread.sleep(50L);
        check(got.size() == 2, "registration and unregistration both delivered");
        check(handbacks.size() == 2 && handbacks.get(0) == handback && handbacks.get(1) == handback,
                "handback is the same instance on every delivery");
        javax.management.MBeanServerNotification first = got.get(0);
        javax.management.MBeanServerNotification second = got.get(1);
        check(javax.management.MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(first.getType()),
                "first notification is registration");
        check(javax.management.MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(second.getType()),
                "second notification is unregistration");
        check(on.equals(first.getMBeanName()) && on.equals(second.getMBeanName()), "getMBeanName exact in both");
        check(javax.management.MBeanServerDelegate.DELEGATE_NAME.equals(first.getSource()),
                "source is the delegate ObjectName");
        check(first.getSequenceNumber() < second.getSequenceNumber(), "sequence numbers strictly increase");
        server.removeNotificationListener(javax.management.MBeanServerDelegate.DELEGATE_NAME, li);
    }

    // ==== appended deepening (2026-11): filter branch ====

    static void deepFilterBranch() throws Exception {
        final javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        final java.util.List<javax.management.Notification> got = new java.util.ArrayList<javax.management.Notification>();
        javax.management.NotificationFilter onlyUnreg = new javax.management.NotificationFilter() {
            public boolean isNotificationEnabled(javax.management.Notification n) {
                return javax.management.MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(n.getType());
            }
        };
        server.addNotificationListener(javax.management.MBeanServerDelegate.DELEGATE_NAME,
                new javax.management.NotificationListener() {
                    public void handleNotification(javax.management.Notification n, Object h) { got.add(n); }
                }, onlyUnreg, null);
        javax.management.ObjectName on = new javax.management.ObjectName("jck.notif:type=Filtered");
        server.registerMBean(new Led(), on);
        server.unregisterMBean(on);
        long deadline = System.currentTimeMillis() + 5000L;
        while (got.isEmpty() && System.currentTimeMillis() < deadline) Thread.sleep(50L);
        check(got.size() == 1, "filter suppresses registration, keeps unregistration only");
        check(javax.management.MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(got.get(0).getType()),
                "the single delivered notification is unregistration");
    }
}

