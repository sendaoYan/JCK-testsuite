/*
 * @test
 * @summary javax.management.AttributeChangeNotificationFilter conformance: white-box structure + black-box behavior.
 * @run main AttributeChangeNotificationFilterTest
 */

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
