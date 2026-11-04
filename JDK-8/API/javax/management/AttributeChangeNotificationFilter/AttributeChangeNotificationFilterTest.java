/*
 * @test
 * @summary javax.management.AttributeChangeNotificationFilter conformance:
 *          full contract coverage. enable/disable attribute names, prefix
 *          semantics, isNotificationEnabled for matching/non-matching name,
 *          wrong type / null type / non-AttributeChangeNotification rejection,
 *          enableAttribute(null) -> IllegalArgumentException, disableAll, and
 *          getEnabledAttributes.
 * @run main AttributeChangeNotificationFilterTest
 */

import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.Notification;

public class AttributeChangeNotificationFilterTest {
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

    static AttributeChangeNotification acn(String name) {
        return new AttributeChangeNotification("src", 1L, 2L, "jmx.attribute.change", name, "int", "old", "new");
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        f.enableAttribute("Count");
        AttributeChangeNotification a = new AttributeChangeNotification("src", 1L, 2L, "jmx.attribute.change", "Count", "int", "old", "new");
        AttributeChangeNotification b = new AttributeChangeNotification("src", 1L, 2L, "jmx.attribute.change", "Other", "int", "old", "new");
        check(f.isNotificationEnabled(a), "enabled attribute");
        check(!f.isNotificationEnabled(b), "disabled attribute");
        check(f.getEnabledAttributes().contains("Count"), "getEnabledAttributes");
    }

    static void testEnableDisable() {
        AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        check(f.getEnabledAttributes().isEmpty(), "initially empty");
        f.enableAttribute("A");
        f.enableAttribute("A");            // duplicate no-op
        check(f.getEnabledAttributes().size() == 1, "duplicate enable no-op");
        f.disableAttribute("A");
        check(!f.isNotificationEnabled(acn("A")), "disabled after disableAttribute");
        f.enableAttribute("A");
        f.enableAttribute("B");
        f.disableAllAttributes();
        check(f.getEnabledAttributes().isEmpty(), "disableAllAttributes");
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() { f.enableAttribute(null); } },
                "enableAttribute(null)");
    }

    static void testRejectsNonMatchingNotification() {
        AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        f.enableAttribute("A");
        check(!f.isNotificationEnabled(new Notification("jmx.attribute.change", "src", 1L)), "non-ACN rejected");
        check(!f.isNotificationEnabled(new Notification("other.type", "src", 1L)), "wrong type rejected");
        check(!f.isNotificationEnabled(acn("B")), "other attribute rejected");
        check(f.isNotificationEnabled(acn("A")), "matching attribute accepted");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testEnableDisable();
        testRejectsNonMatchingNotification();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
