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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;
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

    // ---- deepened (batch d): exact-name semantics / live vector / null / serial form ----

    static void testExactNameSemantics() {
        // matching is exact String.equals on the attribute name: no prefix, no case folding
        AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        f.enableAttribute("Count");
        check(f.isNotificationEnabled(acn("Count")), "exact attribute name matches");
        check(!f.isNotificationEnabled(acn("CountX")), "longer name does not match");
        check(!f.isNotificationEnabled(acn("Cou")), "shorter name does not match (no prefix semantics)");
        check(!f.isNotificationEnabled(acn("count")), "case-sensitive");
        check(!f.isNotificationEnabled(acn("")), "empty attribute name not enabled");
        // an explicitly enabled empty name matches an empty attribute name
        AttributeChangeNotificationFilter e = new AttributeChangeNotificationFilter();
        e.enableAttribute("");
        check(e.isNotificationEnabled(acn("")), "explicitly enabled empty name matches");
        // a plain Notification with the right type string is rejected: must be an ACN instance
        check(!f.isNotificationEnabled(new Notification("jmx.attribute.change", "s", 1L)),
                "non-ACN instance rejected even with matching type string");
    }

    static void testNullNotificationNpe() {
        final AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        f.enableAttribute("A");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() { f.isNotificationEnabled(null); } },
                "isNotificationEnabled(null) -> NPE (impl reads type before instanceof)");
    }

    static void testGetEnabledAttributesIsLive() {
        AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        f.enableAttribute("A");
        f.enableAttribute("B");
        Vector<String> v = f.getEnabledAttributes();
        check(v.size() == 2 && v.contains("A") && v.contains("B"), "getEnabledAttributes content");
        v.remove("A");   // live Vector: the removal reaches the filter's set
        check(!f.isNotificationEnabled(acn("A")),
                "getEnabledAttributes returns the live Vector (removal write-through)");
        check(f.isNotificationEnabled(acn("B")), "untouched name still enabled");
        check(f.getEnabledAttributes().size() == 1, "filter set updated through the vector");
    }

    static void testDisableNoThrowAndNull() {
        AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        f.disableAttribute("never-enabled");
        check(f.getEnabledAttributes().isEmpty(), "disableAttribute of an unknown name is a no-op");
        f.disableAttribute(null);
        check(f.getEnabledAttributes().isEmpty(), "disableAttribute(null) tolerated");
        f.enableAttribute("A");
        f.disableAttribute(null);
        check(f.getEnabledAttributes().size() == 1, "disableAttribute(null) does not disturb enabled names");
    }

    static void testSerializationDeep() throws Exception {
        AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        f.enableAttribute("Count");
        f.enableAttribute("Size");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(f);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        AttributeChangeNotificationFilter back = (AttributeChangeNotificationFilter) ois.readObject();
        ois.close();
        check(back.getEnabledAttributes().size() == 2, "round trip preserves enabled names size");
        check(back.getEnabledAttributes().contains("Count")
                && back.getEnabledAttributes().contains("Size"),
                "round trip preserves enabled names content");
        check(back.isNotificationEnabled(acn("Count")) && back.isNotificationEnabled(acn("Size")),
                "round trip filter still matches enabled names");
        check(!back.isNotificationEnabled(acn("Weight")),
                "round trip filter still rejects other names");
        check(!back.isNotificationEnabled(new Notification("jmx.attribute.change", "s", 1L)),
                "round trip filter still rejects non-ACN instances");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testEnableDisable();
        testRejectsNonMatchingNotification();
        testExactNameSemantics();
        testNullNotificationNpe();
        testGetEnabledAttributesIsLive();
        testDisableNoThrowAndNull();
        testSerializationDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
