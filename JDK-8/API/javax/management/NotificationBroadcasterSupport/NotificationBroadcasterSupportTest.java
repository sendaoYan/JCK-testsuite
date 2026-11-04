/*
 * @test
 * @summary javax.management.NotificationBroadcasterSupport conformance: full
 *          contract coverage. Listener registration (null listener -> IAE),
 *          filter-based delivery, handback pass-through, sendNotification(null)
 *          no-op, listener removal via both overloads incl. ListenerNotFoundException,
 *          getNotificationInfo default/configured clone, and executor construction.
 * @run main NotificationBroadcasterSupportTest
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

public class NotificationBroadcasterSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

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

    static class Recorder implements NotificationListener {
        final AtomicInteger count = new AtomicInteger();
        Notification last;
        Object lastHandback;
        public void handleNotification(Notification n, Object h) {
            count.incrementAndGet();
            last = n;
            lastHandback = h;
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        NotificationBroadcasterSupport nbs = new NotificationBroadcasterSupport();
        final Notification[] received = new Notification[1];
        nbs.addNotificationListener(new NotificationListener() {
            public void handleNotification(Notification n, Object h) { received[0] = n; }
        }, null, null);
        Notification n = new Notification("t", "src", 1L);
        nbs.sendNotification(n);
        check(received[0] == n, "listener received");
        check(nbs.getNotificationInfo() != null, "getNotificationInfo");
    }

    static void testFilterAndHandback() {
        NotificationBroadcasterSupport nbs = new NotificationBroadcasterSupport();
        Recorder r = new Recorder();
        Object handback = new Object();
        NotificationFilter f = new NotificationFilter() {
            public boolean isNotificationEnabled(Notification n) {
                return "keep".equals(n.getType());
            }
        };
        nbs.addNotificationListener(r, f, handback);
        nbs.sendNotification(new Notification("drop", "src", 1L));
        check(r.count.get() == 0, "filtered notification not delivered");
        Notification keep = new Notification("keep", "src", 1L);
        nbs.sendNotification(keep);
        check(r.count.get() == 1 && r.last == keep, "matching notification delivered");
        check(r.lastHandback == handback, "handback pass-through");
    }

    static void testNullListenerAndNullNotification() {
        final NotificationBroadcasterSupport nbs = new NotificationBroadcasterSupport();
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() { nbs.addNotificationListener(null, null, null); } },
                "addNotificationListener(null)");
        Recorder r = new Recorder();
        nbs.addNotificationListener(r, null, null);
        nbs.sendNotification(null);       // no-op, no NPE
        check(r.count.get() == 0, "sendNotification(null) no-op");
    }

    static void testRemoval() throws Exception {
        NotificationBroadcasterSupport nbs = new NotificationBroadcasterSupport();
        Recorder r = new Recorder();
        nbs.addNotificationListener(r, null, null);
        nbs.removeNotificationListener(r);
        nbs.sendNotification(new Notification("t", "src", 1L));
        check(r.count.get() == 0, "listener removed");

        checkThrows(ListenerNotFoundException.class,
                new ThrowingRunnable() { public void run() throws Throwable { nbs.removeNotificationListener(r); } },
                "remove again -> ListenerNotFoundException");

        // exact-match removal (listener+filter+handback)
        NotificationFilter f = new NotificationFilter() {
            public boolean isNotificationEnabled(Notification n) { return true; }
        };
        Object hb = new Object();
        nbs.addNotificationListener(r, f, hb);
        checkThrows(ListenerNotFoundException.class,
                new ThrowingRunnable() { public void run() throws Throwable { nbs.removeNotificationListener(r, null, null); } },
                "remove with mismatched filter/handback -> ListenerNotFoundException");
        nbs.removeNotificationListener(r, f, hb);
        check(r.count.get() == 0, "exact-match removal");
    }

    static void testNotificationInfo() {
        NotificationBroadcasterSupport empty = new NotificationBroadcasterSupport();
        check(empty.getNotificationInfo().length == 0, "default empty info");

        MBeanNotificationInfo info = new MBeanNotificationInfo(new String[]{"a.b"}, "n", "d");
        NotificationBroadcasterSupport configured = new NotificationBroadcasterSupport(info);
        MBeanNotificationInfo[] got = configured.getNotificationInfo();
        check(got.length == 1 && "n".equals(got[0].getName()), "configured info");
        got[0] = null;
        check(configured.getNotificationInfo()[0] != null, "getNotificationInfo returns clone");
    }

    static void testExecutorCtor() {
        Executor e = new Executor() { public void execute(Runnable r) { r.run(); } };
        NotificationBroadcasterSupport nbs = new NotificationBroadcasterSupport(e);
        Recorder r = new Recorder();
        nbs.addNotificationListener(r, null, null);
        Notification n = new Notification("t", "src", 1L);
        nbs.sendNotification(n);
        check(r.count.get() == 1, "executor delivery");
    }

    // ---- deepened (batch d): Broad/CounterMBean emitter closed loop ----

    /**
     * Self-contained standard-MBean surface for the emitter under test.
     * Named BroadMBean so that Broad satisfies the Standard MBean naming
     * convention (interface binary name must be <class name>MBean).
     */
    public static interface BroadMBean {
        int getCount();
        void setCount(int v);
        void reset();
        int add(int x);
    }

    /** An emitter that is also a standard MBean. */
    public static class Broad extends NotificationBroadcasterSupport implements BroadMBean {
        private int count;
        Broad(MBeanNotificationInfo... info) { super(info); }
        public int getCount() { return count; }
        public void setCount(int v) { count = v; }
        public void reset() { count = 0; }
        public int add(int x) { count += x; return count; }
    }

    /** Bounded quiet window used before asserting that nothing arrives. */
    static void waitQuiet(long ms) throws InterruptedException {
        for (long deadline = System.currentTimeMillis() + ms;
             System.currentTimeMillis() < deadline; ) Thread.sleep(20L);
    }

    static void testBroadClosedLoop() throws Exception {
        MBeanNotificationInfo meta = new MBeanNotificationInfo(
                new String[]{"jck.broad"}, Notification.class.getName(), "broad loop");
        Broad b = new Broad(new MBeanNotificationInfo[]{meta});

        // the MBean surface of the emitter still works
        b.setCount(4);
        check(b.getCount() == 4, "Broad counter surface (getCount)");
        check(b.add(3) == 7, "Broad counter surface (add)");

        // injected notification info: content + per-call clone independence
        MBeanNotificationInfo[] info = b.getNotificationInfo();
        check(info.length == 1 && "jck.broad".equals(info[0].getNotifTypes()[0])
                && Notification.class.getName().equals(info[0].getName()),
                "injected MBeanNotificationInfo content");
        info[0] = null;
        check(b.getNotificationInfo()[0] != null, "mutating the returned array does not affect the bean");
        MBeanNotificationInfo[] second = b.getNotificationInfo();
        check(second != info, "each getNotificationInfo call returns a fresh clone");

        // delivery of the identical notification, filter branches, handback identity
        final List<Notification> seen = new ArrayList<Notification>();
        final List<Object> handbacks = new ArrayList<Object>();
        NotificationListener pass = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { seen.add(n); handbacks.add(h); }
        };
        final List<Notification> blocked = new ArrayList<Notification>();
        NotificationListener blocker = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { blocked.add(n); }
        };
        NotificationFilter gate = new NotificationFilter() {
            public boolean isNotificationEnabled(Notification n) {
                return "jck.broad".equals(n.getType());
            }
        };
        Object handback = new Object();
        b.addNotificationListener(pass, gate, handback);
        b.addNotificationListener(blocker, new NotificationFilter() {
            public boolean isNotificationEnabled(Notification n) { return false; }
        }, null);

        Notification sent = new Notification("jck.broad", b, 1L);
        sent.setUserData("payload");
        b.sendNotification(sent);
        check(seen.size() == 1 && seen.get(0) == sent,
                "registered listener receives the identical notification object");
        check(handbacks.size() == 1 && handbacks.get(0) == handback,
                "handback object returned unchanged (same instance)");
        waitQuiet(300L);
        check(blocked.isEmpty(), "filter returning false suppresses delivery (negative wait)");

        // removal: stopped delivery, then ListenerNotFoundException on repeat removal
        b.removeNotificationListener(pass);
        b.sendNotification(new Notification("jck.broad", b, 2L));
        check(seen.size() == 1, "removed listener receives nothing more");
        checkThrows(ListenerNotFoundException.class,
                new ThrowingRunnable() { public void run() throws Exception { b.removeNotificationListener(pass); } },
                "remove unregistered listener -> ListenerNotFoundException");

        // two-param removal requires exact (listener, filter, handback) identity
        final List<Notification> seen2 = new ArrayList<Notification>();
        NotificationListener l = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { seen2.add(n); }
        };
        NotificationFilter f1 = new NotificationFilter() {
            public boolean isNotificationEnabled(Notification n) { return true; }
        };
        NotificationFilter f2 = new NotificationFilter() {
            public boolean isNotificationEnabled(Notification n) { return true; }
        };
        Object h1 = new Object(), h2 = new Object();
        b.addNotificationListener(l, f1, h1);
        checkThrows(ListenerNotFoundException.class,
                new ThrowingRunnable() {
                    public void run() throws Exception { b.removeNotificationListener(l, f2, h2); }
                },
                "remove with mismatched filter+handback -> ListenerNotFoundException");
        b.removeNotificationListener(l, f1, h1);
        b.sendNotification(new Notification("jck.broad", b, 3L));
        check(seen2.isEmpty(), "exact-match removal stops that registration");
        checkThrows(ListenerNotFoundException.class,
                new ThrowingRunnable() {
                    public void run() throws Exception { b.removeNotificationListener(l, f1, h1); }
                },
                "removing an already-removed registration -> ListenerNotFoundException");
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() {
                    public void run() { b.addNotificationListener(null, null, null); }
                },
                "addNotificationListener(null,...) -> IllegalArgumentException");
        b.sendNotification(null);   // documented no-op, must not throw
        waitQuiet(200L);
        check(seen.size() == 1 && seen2.isEmpty() && blocked.isEmpty(),
                "sendNotification(null) is a silent no-op for every registration");
    }

    static void testWildcardRemoval() throws Exception {
        NotificationBroadcasterSupport nbs = new NotificationBroadcasterSupport();
        final List<Object> handbacks = new ArrayList<Object>();
        NotificationListener l = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { handbacks.add(h); }
        };
        Object h1 = new Object(), h2 = new Object();
        nbs.addNotificationListener(l, null, h1);
        nbs.addNotificationListener(l, null, h2);
        nbs.sendNotification(new Notification("t", "s", 1L));
        check(handbacks.size() == 2 && handbacks.contains(h1) && handbacks.contains(h2),
                "same listener registered twice delivers once per registration");
        nbs.removeNotificationListener(l);   // single-arg overload removes all registrations
        nbs.sendNotification(new Notification("t", "s", 2L));
        check(handbacks.size() == 2, "single-arg removal removes every registration (wildcard)");
    }

    static void testExecutorCrossThread() throws Exception {
        Executor perTaskThread = new Executor() {
            public void execute(Runnable r) {
                Thread t = new Thread(r, "jck-nbs-executor");
                t.setDaemon(true);
                t.start();
            }
        };
        final NotificationBroadcasterSupport nbs = new NotificationBroadcasterSupport(perTaskThread);
        final CountDownLatch done = new CountDownLatch(1);
        final Notification[] got = new Notification[1];
        final Object[] hb = new Object[1];
        final Thread[] calledOn = new Thread[1];
        nbs.addNotificationListener(new NotificationListener() {
            public void handleNotification(Notification n, Object h) {
                calledOn[0] = Thread.currentThread();
                got[0] = n;
                hb[0] = h;
                done.countDown();
            }
        }, null, "cross-thread-hb");
        Notification sent = new Notification("jck.exec", nbs, 1L);
        nbs.sendNotification(sent);
        check(done.await(5L, TimeUnit.SECONDS), "delivery through executor within bounded wait");
        check(got[0] == sent, "identical notification delivered across threads");
        check("cross-thread-hb".equals(hb[0]), "handback preserved across threads");
        check(calledOn[0] != Thread.currentThread(),
                "handleNotification ran on the executor thread, not the sender");
    }

    static void testBroadOnMBeanServer() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        Broad b = new Broad(new MBeanNotificationInfo(
                new String[]{"jck.srvbroad"}, Notification.class.getName(), "server loop"));
        ObjectName name = new ObjectName("jck.broad:type=Broad");
        server.registerMBean(b, name);
        b.add(5);
        check(((Integer) server.getAttribute(name, "Count")).intValue() == 5,
                "emitter MBean surface reachable through the MBeanServer");

        final List<Notification> seen = new ArrayList<Notification>();
        final List<Object> hbs = new ArrayList<Object>();
        NotificationListener l = new NotificationListener() {
            public void handleNotification(Notification n, Object h) { seen.add(n); hbs.add(h); }
        };
        Object hb = "server-hb";
        server.addNotificationListener(name, l, null, hb);
        Notification sent = new Notification("jck.srvbroad", name, 1L);
        b.sendNotification(sent);
        check(seen.size() == 1 && seen.get(0) == sent,
                "listener registered via MBeanServer receives the identical notification");
        check("server-hb".equals(hbs.get(0)), "handback via MBeanServer unchanged");
        server.removeNotificationListener(name, l, null, hb);
        b.sendNotification(new Notification("jck.srvbroad", name, 2L));
        check(seen.size() == 1, "removal through MBeanServer stops delivery");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testFilterAndHandback();
        testNullListenerAndNullNotification();
        testRemoval();
        testNotificationInfo();
        testExecutorCtor();
        testBroadClosedLoop();
        testWildcardRemoval();
        testExecutorCrossThread();
        testBroadOnMBeanServer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
