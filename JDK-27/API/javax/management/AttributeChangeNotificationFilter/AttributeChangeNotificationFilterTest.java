/*
 * @test
 * @summary javax.management.AttributeChangeNotificationFilter conformance: white-box structure + black-box behavior.
 * @run main AttributeChangeNotificationFilterTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class AttributeChangeNotificationFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static AttributeChangeNotification notif(String attrName) {
        return new AttributeChangeNotification("s", 1L, 2L, "m", attrName, "int", 0, 1);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.AttributeChangeNotificationFilter");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(NotificationFilter.class.isAssignableFrom(c), "implements NotificationFilter");
        check(hasMethod(c, "enableAttribute"), "has enableAttribute");
        check(hasMethod(c, "disableAttribute"), "has disableAttribute");
        check(hasMethod(c, "disableAllAttributes"), "has disableAllAttributes");
        check(hasMethod(c, "getEnabledAttributes"), "has getEnabledAttributes");
        check(hasMethod(c, "isNotificationEnabled"), "has isNotificationEnabled");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        check(f.getEnabledAttributes().isEmpty(), "starts with no enabled attributes");

        f.enableAttribute("Foo");
        check(f.getEnabledAttributes().size() == 1, "one enabled attribute");
        check(f.getEnabledAttributes().contains("Foo"), "enabled attribute tracked");

        check(f.isNotificationEnabled(notif("Foo")), "matches enabled attribute");
        check(!f.isNotificationEnabled(notif("Bar")), "does not match other attribute");

        f.enableAttribute("Bar");
        check(f.isNotificationEnabled(notif("Foo")) && f.isNotificationEnabled(notif("Bar")), "two enabled attributes");
        check(!f.isNotificationEnabled(notif("Baz")), "third attribute still disabled");

        // case-sensitive matching
        check(!f.isNotificationEnabled(notif("foo")), "case-sensitive");

        f.disableAttribute("Foo");
        check(!f.isNotificationEnabled(notif("Foo")), "disabled attribute no longer matches");
        check(f.isNotificationEnabled(notif("Bar")), "other attribute still enabled");

        f.disableAllAttributes();
        check(f.getEnabledAttributes().isEmpty(), "disableAllAttributes clears");
        check(!f.isNotificationEnabled(notif("Bar")), "nothing matches after clear");

        // null attribute name rejected
        boolean threw = false;
        try { f.enableAttribute(null); } catch (IllegalArgumentException ex) { threw = true; }
        check(threw, "enableAttribute(null) throws IllegalArgumentException");
    }

    // ---- deepened (batch d): exact-name semantics / live vector / null / serial form ----

    static void testExactNameSemantics() {
        // matching is exact String.equals on the attribute name: no prefix, no case folding
        AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        f.enableAttribute("Count");
        check(f.isNotificationEnabled(notif("Count")), "exact attribute name matches");
        check(!f.isNotificationEnabled(notif("CountX")), "longer name does not match");
        check(!f.isNotificationEnabled(notif("Cou")), "shorter name does not match (no prefix semantics)");
        check(!f.isNotificationEnabled(notif("count")), "case-sensitive");
        check(!f.isNotificationEnabled(notif("")), "empty attribute name not enabled");
        // an explicitly enabled empty name matches an empty attribute name
        AttributeChangeNotificationFilter e = new AttributeChangeNotificationFilter();
        e.enableAttribute("");
        check(e.isNotificationEnabled(notif("")), "explicitly enabled empty name matches");
        // a plain Notification with the right type string is rejected: must be an ACN instance
        check(!f.isNotificationEnabled(new Notification("jmx.attribute.change", "s", 1L)),
                "non-ACN instance rejected even with matching type string");
        // ACN for a different attribute name is rejected
        check(!f.isNotificationEnabled(notif("Other")), "other attribute name rejected");
    }

    static void testNullNotificationNpe() {
        AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        f.enableAttribute("A");
        try {
            f.isNotificationEnabled(null);
            check(false, "isNotificationEnabled(null) must throw");
        } catch (NullPointerException ex) {
            check(true, "isNotificationEnabled(null) -> NPE (impl reads type before instanceof)");
        }
    }

    static void testGetEnabledAttributesIsLive() {
        AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        f.enableAttribute("A");
        f.enableAttribute("B");
        Vector<String> v = f.getEnabledAttributes();
        check(v.size() == 2 && v.contains("A") && v.contains("B"), "getEnabledAttributes content");
        v.remove("A");   // live Vector: the removal reaches the filter's set
        check(!f.isNotificationEnabled(notif("A")),
                "getEnabledAttributes returns the live Vector (removal write-through)");
        check(f.isNotificationEnabled(notif("B")), "untouched name still enabled");
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

    static void testEnableNullThrows() {
        AttributeChangeNotificationFilter f = new AttributeChangeNotificationFilter();
        try {
            f.enableAttribute(null);
            check(false, "enableAttribute(null) must throw");
        } catch (IllegalArgumentException ex) {
            check(true, "enableAttribute(null) -> IllegalArgumentException");
        }
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
        check(back.isNotificationEnabled(notif("Count")) && back.isNotificationEnabled(notif("Size")),
                "round trip filter still matches enabled names");
        check(!back.isNotificationEnabled(notif("Weight")),
                "round trip filter still rejects other names");
        check(!back.isNotificationEnabled(new Notification("jmx.attribute.change", "s", 1L)),
                "round trip filter still rejects non-ACN instances");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testExactNameSemantics();
        testNullNotificationNpe();
        testGetEnabledAttributesIsLive();
        testDisableNoThrowAndNull();
        testEnableNullThrows();
        testSerializationDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
