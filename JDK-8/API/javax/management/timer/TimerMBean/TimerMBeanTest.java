/*
 * @test
 * @summary javax.management.timer.TimerMBean conformance: interface reflection.
 * @run main TimerMBeanTest
 */

public class TimerMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.timer.TimerMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "addNotification"), "declares addNotification");
        check(hasMethod(c, "getAllNotificationIDs"), "declares getAllNotificationIDs");
        check(hasMethod(c, "getDate"), "declares getDate");
        testDeepMethodSetExact();
        testDeepThroughInterfaceClosedLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== deepened: shared helpers (batch i, 2026-11) ====

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

    static final long STEP_MS = 25L;

    static class Collector implements javax.management.NotificationListener {
        final java.util.List<javax.management.Notification> received =
                new java.util.concurrent.CopyOnWriteArrayList<javax.management.Notification>();
        public void handleNotification(javax.management.Notification n, Object h) { received.add(n); }
        int count(String type) {
            int c = 0;
            for (javax.management.Notification n : received) if (type.equals(n.getType())) c++;
            return c;
        }
        javax.management.Notification nth(String type, int idx) {
            int i = 0;
            for (javax.management.Notification n : received) {
                if (type.equals(n.getType())) { if (i == idx) return n; i++; }
            }
            return null;
        }
        javax.management.Notification waitFor(String type, int need, long deadlineMs) throws InterruptedException {
            long deadline = System.currentTimeMillis() + deadlineMs;
            while (System.currentTimeMillis() < deadline) {
                if (count(type) >= need) return nth(type, 0);
                Thread.sleep(STEP_MS);
            }
            return nth(type, 0);
        }
        boolean quietFor(String type, long quietMs) throws InterruptedException {
            long deadline = System.currentTimeMillis() + quietMs;
            while (System.currentTimeMillis() < deadline) Thread.sleep(STEP_MS);
            return count(type) == 0;
        }
        // Bounded poll until cond becomes true; false on timeout.
        boolean until(java.util.concurrent.Callable<Boolean> cond, long deadlineMs) throws Exception {
            long deadline = System.currentTimeMillis() + deadlineMs;
            while (System.currentTimeMillis() < deadline) {
                if (cond.call().booleanValue()) return true;
                Thread.sleep(STEP_MS);
            }
            return cond.call().booleanValue();
        }
    }

    static java.util.Set<String> sigs(Class<?> iface) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : iface.getMethods())
            s.add(m.getName() + "/" + m.getParameterTypes().length);
        return s;
    }

    // ==== deepened: exact method set + closed loop through the interface (batch i, 2026-11) ====

    static void testDeepMethodSetExact() throws Exception {
        Class<?> c = Class.forName("javax.management.timer.TimerMBean");
        java.util.Set<String> s = sigs(c);
        String[] expected = {
            "start/0", "stop/0",
            "addNotification/4", "addNotification/5", "addNotification/6", "addNotification/7",
            "removeNotification/1", "removeNotifications/1", "removeAllNotifications/0",
            "getNbNotifications/0", "getAllNotificationIDs/0", "getNotificationIDs/1",
            "getNotificationType/1", "getNotificationMessage/1", "getNotificationUserData/1",
            "getDate/1", "getPeriod/1", "getNbOccurences/1", "getFixedRate/1",
            "getSendPastNotifications/0", "setSendPastNotifications/1",
            "isActive/0", "isEmpty/0"
        };
        for (String e : expected) check(s.contains(e), "TimerMBean declares " + e);
        check(s.size() == 23, "TimerMBean method set exact, got " + s);
    }

    static void testDeepThroughInterfaceClosedLoop() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName timerName = new javax.management.ObjectName("jck.tmb.i:type=timer");
        javax.management.timer.Timer t = new javax.management.timer.Timer();
        server.registerMBean(t, timerName);
        check(t instanceof javax.management.timer.TimerMBean, "instanceof TimerMBean");
        javax.management.timer.TimerMBean ref = t;
        Object ud = new Object();
        java.util.Date d = new java.util.Date(System.currentTimeMillis() + 150L);
        Integer id = ref.addNotification("jck.iface", "m", ud, d);
        check(ref.getNbNotifications() == 1 && !ref.isEmpty(), "bookkeeping via interface");
        check(ref.getAllNotificationIDs().contains(id), "getAllNotificationIDs via interface");
        check(ref.getNotificationIDs("jck.iface").contains(id), "getNotificationIDs via interface");
        check("jck.iface".equals(ref.getNotificationType(id)), "type via interface");
        check("m".equals(ref.getNotificationMessage(id)), "message via interface");
        check(ref.getNotificationUserData(id) == ud, "userData via interface");
        check(ref.getDate(id).getTime() == d.getTime(), "date via interface");
        check(ref.getPeriod(id).longValue() == 0L, "period via interface");
        check(ref.getNbOccurences(id).longValue() == 0L, "occurrences via interface");
        check(Boolean.FALSE.equals(ref.getFixedRate(id)), "fixedRate via interface");
        ref.setSendPastNotifications(true);
        check(ref.getSendPastNotifications(), "sendPastNotifications via interface");
        Collector col = new Collector();
        server.addNotificationListener(timerName, col, null, null);
        ref.start();
        try {
            check(ref.isActive(), "started via interface");
            javax.management.Notification n = col.waitFor("jck.iface", 1, 5000);
            check(n != null, "notification delivered through interface-driven timer");
            check(((javax.management.timer.TimerNotification) n).getNotificationID().equals(id),
                    "delivered id matches");
        } finally {
            ref.stop();
        }
        check(!ref.isActive(), "stopped via interface");
        long drained = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < drained && !ref.isEmpty()) Thread.sleep(STEP_MS);
        check(ref.isEmpty(), "one-shot auto-removed after delivery");
        final javax.management.timer.TimerMBean fref = ref;
        final Integer fid = id;
        checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { fref.removeNotification(fid); }
        }, "removeNotification of already delivered id");
    }
}
