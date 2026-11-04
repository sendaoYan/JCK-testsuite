/*
 * @test
 * @summary javax.management.NotificationFilterSupport conformance: full
 *          contract coverage. enable/disable type prefixes, prefix matching in
 *          isNotificationEnabled, enableType(null) -> IllegalArgumentException,
 *          disableAllTypes, null-type notification handling, and
 *          getEnabledTypes.
 * @run main NotificationFilterSupportTest
 */

import javax.management.Notification;
import javax.management.NotificationFilterSupport;

public class NotificationFilterSupportTest {
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

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType("a.b");
        check(f.isNotificationEnabled(new Notification("a.b", "src", 1L)), "enabled type");
        check(!f.isNotificationEnabled(new Notification("c.d", "src", 1L)), "disabled type");
        check(f.getEnabledTypes().contains("a.b"), "getEnabledTypes");
        f.disableType("a.b");
        check(!f.isNotificationEnabled(new Notification("a.b", "src", 1L)), "disabled after disableType");
    }

    static void testPrefixMatching() {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType("a");
        check(f.isNotificationEnabled(new Notification("a.b.c", "src", 1L)), "prefix match");
        check(f.isNotificationEnabled(new Notification("a2", "src", 1L)), "string-prefix match");
        check(!f.isNotificationEnabled(new Notification("b", "src", 1L)), "non-prefix rejected");
    }

    static void testEnableDisableAll() {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType("x");
        f.enableType("x");               // duplicate no-op
        check(f.getEnabledTypes().size() == 1, "duplicate enable no-op");
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() { f.enableType(null); } },
                "enableType(null)");
        f.enableType("y");
        f.disableAllTypes();
        check(f.getEnabledTypes().isEmpty(), "disableAllTypes");
        check(!f.isNotificationEnabled(new Notification("x", "src", 1L)), "disabled after disableAllTypes");
    }

    static void testNullTypeNotification() {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType("a");
        check(!f.isNotificationEnabled(new Notification(null, "src", 1L)), "null type rejected");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testPrefixMatching();
        testEnableDisableAll();
        testNullTypeNotification();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
