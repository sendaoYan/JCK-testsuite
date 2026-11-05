/*
 * @test
 * @summary javax.management.timer.TimerNotification conformance: white-box structure + black-box behavior.
 * @run main TimerNotificationTest
 */

import javax.management.Notification;
import javax.management.timer.TimerNotification;

public class TimerNotificationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.timer.TimerNotification");
        check(Notification.class.isAssignableFrom(c), "extends Notification");
        check(c.getConstructors().length == 1, "single public constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Object source = new Object();
        TimerNotification n = new TimerNotification("type", source, 1L, 100L, "msg", Integer.valueOf(42));
        check("type".equals(n.getType()), "getType inherited");
        check(n.getSequenceNumber() == 1L, "getSequenceNumber inherited");
        check(n.getTimeStamp() == 100L, "getTimeStamp inherited");
        check("msg".equals(n.getMessage()), "getMessage inherited");
        check(source == n.getSource(), "getSource inherited");
        check(Integer.valueOf(42).equals(n.getNotificationID()), "getNotificationID");

        // userData is inherited from Notification and settable.
        n.setUserData("payload");
        check("payload".equals(n.getUserData()), "setUserData/getUserData");

        // A null id is representable.
        TimerNotification n2 = new TimerNotification("t2", source, 2L, 200L, "m2", null);
        check(n2.getNotificationID() == null, "null notification id preserved");
        check("t2".equals(n2.getType()), "second type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepConstructorGetters();
        testDeepInheritedMutators();
        testDeepSerialization();
        testDeepRealDeliveryIntegration();
        testDeepTimerAlarmClockNotificationWhiteBox();
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

    // ==== deepened: constructor getters, serialization, real delivery (batch i, 2026-11) ====

    static void testDeepConstructorGetters() throws Exception {
        Object src = new Object();
        javax.management.timer.TimerNotification n =
                new javax.management.timer.TimerNotification("a.b", src, 42L, 99L, "hello", 7);
        check(n.getNotificationID() != null && n.getNotificationID().intValue() == 7, "getNotificationID");
        check("a.b".equals(n.getType()), "type");
        check(src == n.getSource(), "source");
        check(n.getSequenceNumber() == 42L, "sequence number");
        check(n.getTimeStamp() == 99L, "time stamp");
        check("hello".equals(n.getMessage()), "message");
        check(n.getUserData() == null, "userData null by default");
    }

    static void testDeepInheritedMutators() throws Exception {
        javax.management.timer.TimerNotification n =
                new javax.management.timer.TimerNotification("a.b", "s", 0L, 0L, "m", 1);
        Object src2 = new Object();
        n.setSource(src2);
        check(src2 == n.getSource(), "setSource round-trip");
        n.setSequenceNumber(11L);
        check(n.getSequenceNumber() == 11L, "setSequenceNumber round-trip");
        n.setTimeStamp(12345L);
        check(n.getTimeStamp() == 12345L, "setTimeStamp round-trip");
        Object ud = new Object();
        n.setUserData(ud);
        check(ud == n.getUserData(), "setUserData round-trip");
        check(n.toString().contains("a.b"), "toString contains type");
    }

    static void testDeepSerialization() throws Exception {
        javax.management.timer.TimerNotification orig =
                new javax.management.timer.TimerNotification("a.b", "src", 42L, 99L, "hello", 7);
        orig.setUserData("ud");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        javax.management.timer.TimerNotification copy = (javax.management.timer.TimerNotification) ois.readObject();
        check(copy.getNotificationID().intValue() == 7, "id survives serialization");
        check("a.b".equals(copy.getType()), "type survives serialization");
        check("src".equals(copy.getSource()), "source survives serialization");
        check(copy.getSequenceNumber() == 42L, "sequence survives serialization");
        check(copy.getTimeStamp() == 99L, "time stamp survives serialization");
        check("hello".equals(copy.getMessage()), "message survives serialization");
        check("ud".equals(copy.getUserData()), "userData survives serialization");
        check(java.io.ObjectStreamClass.lookup(javax.management.timer.TimerNotification.class)
                .getSerialVersionUID() == 1798492029603825750L, "serialVersionUID locked");
        javax.management.timer.TimerNotification other =
                new javax.management.timer.TimerNotification("a.b", "src", 42L, 99L, "hello", 7);
        check(!orig.equals(other), "TimerNotification equality is identity (Notification contract)");
    }

    static void testDeepRealDeliveryIntegration() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.ObjectName timerName = new javax.management.ObjectName("jck.tn.i:type=timer");
        javax.management.timer.Timer t = new javax.management.timer.Timer();
        server.registerMBean(t, timerName);
        Collector col = new Collector();
        server.addNotificationListener(timerName, col, null, null);
        Object ud = "payload";
        Integer id = t.addNotification("jck.tn", "m", ud,
                new java.util.Date(System.currentTimeMillis() + 120L));
        t.start();
        try {
            javax.management.Notification n = col.waitFor("jck.tn", 1, 5000);
            check(n != null, "timer notification delivered");
            javax.management.timer.TimerNotification tn = (javax.management.timer.TimerNotification) n;
            check(tn.getNotificationID().equals(id), "delivered notification keeps its id");
            check("jck.tn".equals(tn.getType()), "delivered type");
            check("payload".equals(tn.getUserData()), "delivered userData");
            check(tn.getTimeStamp() > 0L, "delivered time stamp set by the timer");
        } finally {
            t.stop();
        }
    }

    // White box: package-private helper notification class of the timer machinery.
    static void testDeepTimerAlarmClockNotificationWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.timer.TimerAlarmClockNotification");
        check(!java.lang.reflect.Modifier.isPublic(c.getModifiers()),
                "TimerAlarmClockNotification is package-private");
        check(javax.management.Notification.class.isAssignableFrom(c), "extends Notification");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1, "single constructor");
        check(java.lang.reflect.Modifier.isPublic(ctors[0].getModifiers()), "constructor public");
        check(ctors[0].getParameterTypes().length == 1
                && ctors[0].getParameterTypes()[0].getName().equals("javax.management.timer.TimerAlarmClock"),
                "constructor takes the alarm clock");
        check(c.getDeclaredMethods().length == 0, "declares no own methods");
    }
}
