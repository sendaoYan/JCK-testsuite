/*
 * @test
 * @summary javax.management.NotificationBroadcasterSupport conformance: full
 *          contract coverage. Listener registration (null listener -> IAE),
 *          filter-based delivery, handback pass-through, sendNotification(null)
 *          no-op, listener removal via both overloads incl. ListenerNotFoundException,
 *          getNotificationInfo default/configured clone, and executor construction.
 * @run main NotificationBroadcasterSupportTest
 */

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.MBeanNotificationInfo;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

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

    public static void main(String[] args) throws Exception {
        testBasic();
        testFilterAndHandback();
        testNullListenerAndNullNotification();
        testRemoval();
        testNotificationInfo();
        testExecutorCtor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
