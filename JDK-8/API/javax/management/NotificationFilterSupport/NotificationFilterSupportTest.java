/*
 * @test
 * @summary javax.management.NotificationFilterSupport conformance: full
 *          contract coverage. enable/disable type prefixes, prefix matching in
 *          isNotificationEnabled, enableType(null) -> IllegalArgumentException,
 *          disableAllTypes, null-type notification handling, and
 *          getEnabledTypes.
 * @run main NotificationFilterSupportTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;
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

    // ---- deepened (batch d): prefix semantics locked / live vector / null / serial form ----

    static void testPrefixSemanticsLocked() {
        // matching is String.startsWith on the enabled prefix (no wildcard, no suffix)
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType("jck.");
        check(f.isNotificationEnabled(new Notification("jck.x", "s", 1L)), "prefix 'jck.' matches 'jck.x'");
        check(f.isNotificationEnabled(new Notification("jck.", "s", 1L)), "prefix matches the exact type too");
        check(!f.isNotificationEnabled(new Notification("jck", "s", 1L)), "shorter type is not matched");
        check(!f.isNotificationEnabled(new Notification("other.jck.x", "s", 1L)),
                "no substring/suffix semantics");

        // '*' is a literal character, not a wildcard
        NotificationFilterSupport g = new NotificationFilterSupport();
        g.enableType("jck.*");
        check(!g.isNotificationEnabled(new Notification("jck.x", "s", 1L)), "'*' is literal (no wildcard)");
        check(g.isNotificationEnabled(new Notification("jck.*", "s", 1L)), "literal 'jck.*' matches itself");

        // empty prefix enables every non-null type
        NotificationFilterSupport e = new NotificationFilterSupport();
        e.enableType("");
        check(e.isNotificationEnabled(new Notification("anything", "s", 1L)),
                "empty prefix matches every type");
        check(!e.isNotificationEnabled(new Notification(null, "s", 1L)),
                "null type is always rejected");

        // enabling a longer prefix does not enable a shorter one
        NotificationFilterSupport h = new NotificationFilterSupport();
        h.enableType("jck.one.two");
        check(!h.isNotificationEnabled(new Notification("jck.one", "s", 1L)),
                "enabling a longer prefix does not match a shorter type");
        check(h.isNotificationEnabled(new Notification("jck.one.two.three", "s", 1L)),
                "longer type still matches the enabled prefix");
    }

    static void testNullNotificationNpe() {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType("a");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() { f.isNotificationEnabled(null); } },
                "isNotificationEnabled(null) -> NPE (impl dereferences getType)");
    }

    static void testGetEnabledTypesIsLive() {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType("a");
        Vector<String> v = f.getEnabledTypes();
        check(v.size() == 1 && "a".equals(v.get(0)), "getEnabledTypes content");
        v.add("b");   // live Vector: the write reaches the filter's set
        check(f.getEnabledTypes().size() == 2, "vector write-through visible in getEnabledTypes");
        check(f.isNotificationEnabled(new Notification("b.c", "s", 1L)),
                "getEnabledTypes returns the live Vector (write-through to matching)");
    }

    static void testDisableTypeNoThrow() {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.disableType("never-enabled");
        check(f.getEnabledTypes().isEmpty(), "disableType of a never-enabled type is a no-op");
        f.disableType(null);
        check(f.getEnabledTypes().isEmpty(), "disableType(null) tolerated");
        f.enableType("a");
        f.disableType(null);
        check(f.getEnabledTypes().size() == 1, "disableType(null) does not disturb enabled types");
    }

    static void testSerializationDeep() throws Exception {
        NotificationFilterSupport f = new NotificationFilterSupport();
        f.enableType("jck.one");
        f.enableType("jck.two");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(f);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        NotificationFilterSupport back = (NotificationFilterSupport) ois.readObject();
        ois.close();
        check(back.getEnabledTypes().size() == 2, "round trip preserves enabled set size");
        check(back.getEnabledTypes().contains("jck.one") && back.getEnabledTypes().contains("jck.two"),
                "round trip preserves enabled set content");
        check(back.isNotificationEnabled(new Notification("jck.one.x", "s", 1L)),
                "round trip filter still matches enabled prefix");
        check(!back.isNotificationEnabled(new Notification("jck.three", "s", 1L)),
                "round trip filter still rejects other types");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testPrefixMatching();
        testEnableDisableAll();
        testNullTypeNotification();
        testPrefixSemanticsLocked();
        testNullNotificationNpe();
        testGetEnabledTypesIsLive();
        testDisableTypeNoThrow();
        testSerializationDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
