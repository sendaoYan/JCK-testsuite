/*
 * @test
 * @summary javax.management.timer.Timer conformance: white-box structure + black-box behavior.
 * @run main TimerTest
 */

import java.util.Collections;
import java.util.Date;
import java.util.Vector;
import javax.management.InstanceNotFoundException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.timer.Timer;
import javax.management.timer.TimerMBean;

public class TimerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static Vector<Integer> sorted(Vector<Integer> v) {
        Vector<Integer> c = new Vector<>(v);
        Collections.sort(c);
        return c;
    }
    // A fixed date in the future (2100-01-01T00:00:00Z) so addNotification does
    // not clamp it to the wall clock and getDate stays deterministic.
    static final long FUTURE_MILLIS = 4102444800000L;

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.timer.Timer");
        check(!c.isInterface(), "not interface");
        check(NotificationBroadcasterSupport.class.isAssignableFrom(c),
                "extends NotificationBroadcasterSupport");
        check(TimerMBean.class.isAssignableFrom(c), "implements TimerMBean");
        check(javax.management.MBeanRegistration.class.isAssignableFrom(c), "implements MBeanRegistration");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Timer t = new Timer();
        check(t.isEmpty(), "initially empty");
        check(t.getNbNotifications() == 0, "zero notifications");
        check(!t.isActive(), "not active");
        check(!t.getSendPastNotifications(), "default sendPastNotifications false");

        Date future = new Date(FUTURE_MILLIS);

        Integer id1 = t.addNotification("typeA", "msgA", "userA", future);
        check(id1 != null, "once id non-null");
        check(t.getNbNotifications() == 1, "one notification");

        Integer id2 = t.addNotification("typeB", "msgB", "userB", future, 500L);
        Integer id3 = t.addNotification("typeA", "msgC", "userC", future, 1000L, 5L);
        Integer id4 = t.addNotification("typeA", "msgD", "userD", future, 2000L, 3L, true);
        check(t.getNbNotifications() == 4, "four notifications");

        // IDs are distinct.
        check(!id1.equals(id2) && !id2.equals(id3) && !id3.equals(id4), "ids distinct");

        // getAllNotificationIDs (sort before comparing)
        Vector<Integer> all = sorted(t.getAllNotificationIDs());
        check(all.size() == 4, "all ids size 4");
        check(all.contains(id1) && all.contains(id2) && all.contains(id3) && all.contains(id4),
                "all ids contains every id");

        // getNotificationIDs by type
        Vector<Integer> typeA = sorted(t.getNotificationIDs("typeA"));
        check(typeA.size() == 3 && typeA.contains(id1) && typeA.contains(id3) && typeA.contains(id4),
                "typeA ids");
        Vector<Integer> typeB = sorted(t.getNotificationIDs("typeB"));
        check(typeB.size() == 1 && typeB.contains(id2), "typeB ids");
        check(t.getNotificationIDs("missing").isEmpty(), "no ids for missing type");

        // per-notification queries
        check("typeA".equals(t.getNotificationType(id1)), "notification type");
        check("msgA".equals(t.getNotificationMessage(id1)), "notification message");
        check("userA".equals(t.getNotificationUserData(id1)), "notification user data");
        check(new Date(FUTURE_MILLIS).equals(t.getDate(id1)), "once date unchanged (future)");
        check(t.getPeriod(id1).longValue() == 0L, "once period 0");
        check(t.getNbOccurences(id1).longValue() == 0L, "once occurrences 0");
        check(!t.getFixedRate(id1).booleanValue(), "once fixed rate false");

        check(t.getPeriod(id2).longValue() == 500L, "period 500");
        check(t.getPeriod(id3).longValue() == 1000L, "period 1000");
        check(t.getNbOccurences(id3).longValue() == 5L, "occurrences 5");
        check(!t.getFixedRate(id3).booleanValue(), "fixed rate false for id3");
        check(t.getPeriod(id4).longValue() == 2000L, "period 2000");
        check(t.getNbOccurences(id4).longValue() == 3L, "occurrences 3");
        check(t.getFixedRate(id4).booleanValue(), "fixed rate true for id4");

        // missing id queries return null
        check(t.getNotificationType(Integer.valueOf(-999)) == null, "missing type null");

        // setSendPastNotifications
        t.setSendPastNotifications(false);
        check(!t.getSendPastNotifications(), "sendPastNotifications false");
        t.setSendPastNotifications(true);
        check(t.getSendPastNotifications(), "sendPastNotifications true");

        // removeNotification
        t.removeNotification(id1);
        check(t.getNbNotifications() == 3, "three after removeNotification");
        boolean nfe1 = false;
        try { t.removeNotification(id1); } catch (InstanceNotFoundException e) { nfe1 = true; }
        check(nfe1, "removeNotification missing throws InstanceNotFoundException");

        // removeNotifications(type)
        t.removeNotifications("typeB");
        check(t.getNbNotifications() == 2, "two after removeNotifications(typeB)");
        boolean nfe2 = false;
        try { t.removeNotifications("typeB"); } catch (InstanceNotFoundException e) { nfe2 = true; }
        check(nfe2, "removeNotifications missing type throws InstanceNotFoundException");

        // removeAllNotifications
        t.removeAllNotifications();
        check(t.isEmpty(), "empty after removeAllNotifications");
        check(t.getNbNotifications() == 0, "zero after removeAllNotifications");

        // validation
        boolean iae1 = false;
        try { t.addNotification("t", "m", "u", null); } catch (IllegalArgumentException e) { iae1 = true; }
        check(iae1, "addNotification null date throws IllegalArgumentException");
        boolean iae2 = false;
        try { t.addNotification("t", "m", "u", future, -1L); } catch (IllegalArgumentException e) { iae2 = true; }
        check(iae2, "addNotification negative period throws IllegalArgumentException");

        // constants
        check(Timer.ONE_SECOND == 1000L, "ONE_SECOND");
        check(Timer.ONE_MINUTE == 60000L, "ONE_MINUTE");
        check(Timer.ONE_HOUR == 3600000L, "ONE_HOUR");
        check(Timer.ONE_DAY == 86400000L, "ONE_DAY");
        check(Timer.ONE_WEEK == 604800000L, "ONE_WEEK");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepOneShotDelivery();
        testDeepPeriodicInfinite();
        testDeepFiniteOccurrences();
        testDeepPastNotificationSemantics();
        testDeepStopStartPending();
        testDeepRemovalAndIdReset();
        testDeepViaServerAttributes();
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

    // ==== deepened: real delivery closed loop (batch i, 2026-11) ====

    // One-shot future notification: exact bookkeeping, delivery payload, auto-removal.
    static void testDeepOneShotDelivery() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName timerName = new javax.management.ObjectName("jck.timer.i:type=one");
        javax.management.timer.Timer t = new javax.management.timer.Timer();
        server.registerMBean(t, timerName);
        Collector col = new Collector();
        server.addNotificationListener(timerName, col, null, null);
        Object ud = new Object();
        java.util.Date d = new java.util.Date(System.currentTimeMillis() + 150L);
        Integer id = t.addNotification("jck.oneshot", "hello", ud, d);
        check(id.intValue() == 1, "first notification id is 1");
        check(t.getDate(id).getTime() == d.getTime(), "getDate keeps the exact millisecond");
        check(t.getPeriod(id).longValue() == 0L, "one-shot period 0");
        check(t.getNbOccurences(id).longValue() == 0L, "one-shot occurrences 0");
        check(Boolean.FALSE.equals(t.getFixedRate(id)), "one-shot fixedRate false");
        check("jck.oneshot".equals(t.getNotificationType(id)), "type round-trip");
        check("hello".equals(t.getNotificationMessage(id)), "message round-trip");
        check(t.getNotificationUserData(id) == ud, "userData same instance round-trip");
        t.start();
        try {
            javax.management.Notification n = col.waitFor("jck.oneshot", 1, 5000);
            check(n != null, "one-shot delivered");
            javax.management.timer.TimerNotification tn = (javax.management.timer.TimerNotification) n;
            check(n instanceof javax.management.timer.TimerNotification, "delivered as TimerNotification");
            check(tn.getNotificationID().equals(id), "notification id matches add return");
            check(n.getSequenceNumber() == 1L, "first timer notification sequence number is 1");
            check(timerName.equals(n.getSource()), "source is the timer ObjectName");
            check(n.getTimeStamp() >= d.getTime(), "time stamp not before the scheduled date");
            check("hello".equals(n.getMessage()), "message delivered");
            check(n.getUserData() == ud, "userData same instance delivered");
            // one-shot is removed from the list after delivery
            boolean drained = false;
            long deadline = System.currentTimeMillis() + 5000;
            while (System.currentTimeMillis() < deadline && !drained) {
                if (t.isEmpty() && t.getNbNotifications() == 0) drained = true;
                else Thread.sleep(STEP_MS);
            }
            check(drained, "one-shot auto-removed after delivery");
            check(t.getNotificationType(id) == null, "getter on removed id returns null");
        } finally {
            t.stop();
        }
    }

    // Infinite periodic notification: >=2 deliveries, advancing date, removal by id.
    static void testDeepPeriodicInfinite() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName timerName = new javax.management.ObjectName("jck.timer.i:type=periodic");
        javax.management.timer.Timer t = new javax.management.timer.Timer();
        server.registerMBean(t, timerName);
        Collector col = new Collector();
        server.addNotificationListener(timerName, col, null, null);
        java.util.Date d = new java.util.Date(System.currentTimeMillis() + 100L);
        Integer id = t.addNotification("jck.periodic", "m", "u", d, 50L);
        check(t.getPeriod(id).longValue() == 50L, "period stored");
        check(t.getNbOccurences(id).longValue() == 0L, "5-arg addNotification defaults to infinite occurrences");
        check(Boolean.FALSE.equals(t.getFixedRate(id)), "periodic default fixedRate false");
        t.start();
        try {
            javax.management.Notification n = col.waitFor("jck.periodic", 2, 5000);
            check(n != null, "at least two periodic deliveries");
            javax.management.Notification first = col.nth("jck.periodic", 0);
            javax.management.Notification second = col.nth("jck.periodic", 1);
            check(first.getSequenceNumber() < second.getSequenceNumber(), "sequence numbers increase");
            check(t.getDate(id).getTime() > d.getTime(), "scheduled date advances with the period");
            t.removeNotification(id);
            check(t.getNbNotifications() == 0 && t.isEmpty(), "removed by id while running");
            final javax.management.timer.Timer ft = t;
            final Integer fid = id;
            checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
                public void run() throws Exception { ft.removeNotification(fid); }
            }, "removeNotification twice");
            try {
                t.removeNotification(id);
                throw new AssertionError("should have thrown");
            } catch (javax.management.InstanceNotFoundException e) {
                check(e.getMessage() != null && e.getMessage().contains("Timer notification to remove"),
                        "remove-by-id message, got: " + e.getMessage());
            }
        } finally {
            t.stop();
        }
    }

    // Finite occurrences (nbOccurences=2): exactly two deliveries, then auto-removal.
    static void testDeepFiniteOccurrences() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName timerName = new javax.management.ObjectName("jck.timer.i:type=finite");
        javax.management.timer.Timer t = new javax.management.timer.Timer();
        server.registerMBean(t, timerName);
        Collector col = new Collector();
        server.addNotificationListener(timerName, col, null, null);
        t.addNotification("jck.finite", "m", null,
                new java.util.Date(System.currentTimeMillis() + 50L), 60L, 2L);
        t.start();
        try {
            long deadline = System.currentTimeMillis() + 5000;
            while (System.currentTimeMillis() < deadline
                    && !(t.isEmpty() && col.count("jck.finite") >= 2)) {
                Thread.sleep(STEP_MS);
            }
            check(t.isEmpty(), "finite periodic auto-removed after last occurrence");
            int delivered = col.count("jck.finite");
            long quiet = System.currentTimeMillis() + 500;
            while (System.currentTimeMillis() < quiet) Thread.sleep(STEP_MS);
            check(col.count("jck.finite") == delivered, "no delivery beyond the occurrence budget");
            check(col.count("jck.finite") == 2, "exactly two deliveries, got " + col.count("jck.finite"));
        } finally {
            t.stop();
        }
    }

    // Past-date semantics: clamped to now; dropped silently at start with sendPast=false
    // (after the date has clearly passed), delivered with sendPast=true; immediate
    // delivery when added while the timer is active.
    static void testDeepPastNotificationSemantics() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();

        // (a) past one-shot added while inactive, sendPastNotifications=false
        javax.management.ObjectName tn1 = new javax.management.ObjectName("jck.timer.i:type=past1");
        javax.management.timer.Timer t1 = new javax.management.timer.Timer();
        server.registerMBean(t1, tn1);
        Collector col1 = new Collector();
        server.addNotificationListener(tn1, col1, null, null);
        java.util.Date past = new java.util.Date(System.currentTimeMillis() - 5000L);
        long pastMs = past.getTime(); // addNotification clamps the Date in place
        Integer id1 = t1.addNotification("jck.past1", "m", null, past);
        check(t1.getDate(id1).getTime() > pastMs,
                "past date is clamped up to the add time");
        check(past.getTime() > pastMs,
                "addNotification rewrote the caller's Date object to the clamped time");
        Thread.sleep(300); // make sure the clamped date is strictly before start
        t1.start();
        try {
            check(col1.quietFor("jck.past1", 900), "sendPast=false: past one-shot is dropped, not delivered");
            check(t1.isEmpty(), "dropped one-shot is removed from the list");
        } finally {
            t1.stop();
        }

        // (b) same setup with sendPastNotifications=true
        javax.management.ObjectName tn2 = new javax.management.ObjectName("jck.timer.i:type=past2");
        javax.management.timer.Timer t2 = new javax.management.timer.Timer();
        server.registerMBean(t2, tn2);
        t2.setSendPastNotifications(true);
        check(t2.getSendPastNotifications(), "sendPastNotifications round-trip");
        Collector col2 = new Collector();
        server.addNotificationListener(tn2, col2, null, null);
        t2.addNotification("jck.past2", "m", null, new java.util.Date(System.currentTimeMillis() - 5000L));
        Thread.sleep(300);
        t2.start();
        try {
            javax.management.Notification n = col2.waitFor("jck.past2", 1, 5000);
            check(n != null, "sendPast=true: past one-shot delivered at start");
            check(t2.isEmpty(), "delivered past one-shot is auto-removed");
        } finally {
            t2.stop();
            t2.setSendPastNotifications(false);
        }

        // (c) past date added while the timer is active fires immediately
        javax.management.ObjectName tn3 = new javax.management.ObjectName("jck.timer.i:type=past3");
        javax.management.timer.Timer t3 = new javax.management.timer.Timer();
        server.registerMBean(t3, tn3);
        Collector col3 = new Collector();
        server.addNotificationListener(tn3, col3, null, null);
        t3.start();
        try {
            t3.addNotification("jck.past3", "m", null, new java.util.Date(System.currentTimeMillis() - 100L));
            javax.management.Notification n = col3.waitFor("jck.past3", 1, 5000);
            check(n != null, "past date added while active is delivered immediately");
        } finally {
            t3.stop();
        }
    }

    // stop() cancels pending alarms; start() delivers the still-pending notification.
    static void testDeepStopStartPending() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName timerName = new javax.management.ObjectName("jck.timer.i:type=pend");
        javax.management.timer.Timer t = new javax.management.timer.Timer();
        server.registerMBean(t, timerName);
        Collector col = new Collector();
        server.addNotificationListener(timerName, col, null, null);
        t.addNotification("jck.pend", "m", null, new java.util.Date(System.currentTimeMillis() + 2500L));
        t.start();
        t.stop();
        check(col.quietFor("jck.pend", 1000), "nothing delivered while stopped");
        check(!t.isEmpty(), "pending notification survives stop");
        t.start();
        try {
            javax.management.Notification n = col.waitFor("jck.pend", 1, 5000);
            check(n != null, "pending notification delivered after restart");
        } finally {
            t.stop();
        }
        // addNotification is allowed while inactive
        Integer late = t.addNotification("jck.late", "m", null,
                new java.util.Date(System.currentTimeMillis() + 3600000L));
        check(!t.isEmpty() && t.getNbNotifications() == 1, "addNotification while inactive is stored");
        check(t.getNotificationType(late) != null, "stored notification is queryable");
        t.removeAllNotifications();
        // start/stop idempotency
        t.start();
        t.start();
        check(t.isActive(), "start twice idempotent");
        t.stop();
        t.stop();
        check(!t.isActive(), "stop twice idempotent");
    }

    // removeNotifications(type), removeAllNotifications, id counter reset, null getters.
    static void testDeepRemovalAndIdReset() throws Exception {
        javax.management.timer.Timer t = new javax.management.timer.Timer();
        java.util.Date far = new java.util.Date(System.currentTimeMillis() + 3600000L);
        Integer a1 = t.addNotification("jck.rtype", "m1", null, far);
        Integer a2 = t.addNotification("jck.rtype", "m2", null, far);
        Integer b1 = t.addNotification("jck.rother", "m3", null, far);
        java.util.Vector<Integer> byType = t.getNotificationIDs("jck.rtype");
        check(byType.size() == 2 && byType.contains(a1) && byType.contains(a2),
                "getNotificationIDs(type) groups ids");
        t.removeNotifications("jck.rtype");
        check(t.getNbNotifications() == 1, "removeNotifications(type) clears all of the type");
        java.util.Vector<Integer> all = t.getAllNotificationIDs();
        check(all.size() == 1 && all.contains(b1), "other type survives");
        final javax.management.timer.Timer ft = t;
        checkThrows(javax.management.InstanceNotFoundException.class, new ThrowingRunnable() {
            public void run() throws Exception { ft.removeNotifications("jck.rtype"); }
        }, "removeNotifications(unknown type)");
        try {
            t.removeNotifications("jck.nothere");
            throw new AssertionError("should have thrown");
        } catch (javax.management.InstanceNotFoundException e) {
            check(e.getMessage() != null && e.getMessage().contains("Timer notifications to remove"),
                    "remove-by-type message, got: " + e.getMessage());
        }
        t.removeAllNotifications();
        check(t.isEmpty() && t.getNbNotifications() == 0, "removeAllNotifications clears everything");
        check(t.getAllNotificationIDs().isEmpty(), "id vector empty");
        Integer fresh = t.addNotification("jck.next", "m", null, far);
        check(fresh.intValue() == 1, "id counter restarts at 1 after removeAllNotifications, got " + fresh);
        t.removeNotification(fresh);
        check(t.getNotificationType(fresh) == null && t.getDate(fresh) == null
                && t.getPeriod(fresh) == null && t.getNbOccurences(fresh) == null
                && t.getFixedRate(fresh) == null && t.getNotificationUserData(fresh) == null
                && t.getNotificationMessage(fresh) == null,
                "all getters on removed id return null");
    }

    // Timer MBean attributes as seen through the MBeanServer.
    static void testDeepViaServerAttributes() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName timerName = new javax.management.ObjectName("jck.timer.i:type=attrs");
        javax.management.timer.Timer t = new javax.management.timer.Timer();
        server.registerMBean(t, timerName);
        javax.management.MBeanInfo info = server.getMBeanInfo(timerName);
        java.util.Set<String> attrs = new java.util.HashSet<String>();
        for (javax.management.MBeanAttributeInfo a : info.getAttributes()) attrs.add(a.getName());
        check(attrs.contains("Active") && attrs.contains("NbNotifications")
                && attrs.contains("Empty") && attrs.contains("SendPastNotifications"),
                "Timer exposes Active/NbNotifications/Empty/SendPastNotifications, got " + attrs);
        server.setAttribute(timerName, new javax.management.Attribute("SendPastNotifications", Boolean.TRUE));
        check(Boolean.TRUE.equals(server.getAttribute(timerName, "SendPastNotifications")),
                "SendPastNotifications writable through server");
        check(Integer.valueOf(0).equals(server.getAttribute(timerName, "NbNotifications")),
                "NbNotifications readable through server");
        check(Boolean.FALSE.equals(server.getAttribute(timerName, "Active")), "Active false before start");
        t.start();
        check(Boolean.TRUE.equals(server.getAttribute(timerName, "Active")), "Active true after start");
        t.stop();
        check(Boolean.FALSE.equals(server.getAttribute(timerName, "Active")), "Active false after stop");
    }
}
