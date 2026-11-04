/*
 * @test
 * @summary javax.management.NotificationFilterSupport conformance: white-box structure + black-box behavior.
 * @run main NotificationFilterSupportTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class NotificationFilterSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.NotificationFilterSupport");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(NotificationFilter.class.isAssignableFrom(c), "implements NotificationFilter");
        check(hasMethod(c, "enableType"), "has enableType");
        check(hasMethod(c, "disableType"), "has disableType");
        check(hasMethod(c, "disableAllTypes"), "has disableAllTypes");
        check(hasMethod(c, "getEnabledTypes"), "has getEnabledTypes");
        check(hasMethod(c, "isNotificationEnabled"), "has isNotificationEnabled");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        NotificationFilterSupport f = new NotificationFilterSupport();
        check(f.getEnabledTypes().isEmpty(), "starts empty");

        f.enableType("type.one");
        check(f.getEnabledTypes().size() == 1 && f.getEnabledTypes().contains("type.one"), "enableType tracks");
        check(f.isNotificationEnabled(new Notification("type.one", "s", 1L)), "matches enabled type");
        check(!f.isNotificationEnabled(new Notification("type.two", "s", 1L)), "does not match other type");

        f.enableType("type.two");
        check(f.isNotificationEnabled(new Notification("type.one", "s", 1L))
                && f.isNotificationEnabled(new Notification("type.two", "s", 1L)), "two types enabled");
        check(!f.isNotificationEnabled(new Notification("type.three", "s", 1L)), "third type disabled");

        // case-sensitive
        check(!f.isNotificationEnabled(new Notification("TYPE.ONE", "s", 1L)), "case-sensitive");

        f.disableType("type.one");
        check(!f.isNotificationEnabled(new Notification("type.one", "s", 1L)), "disabled type no longer matches");
        check(f.isNotificationEnabled(new Notification("type.two", "s", 1L)), "other type still enabled");

        f.disableAllTypes();
        check(f.getEnabledTypes().isEmpty(), "disableAllTypes clears");
        check(!f.isNotificationEnabled(new Notification("type.two", "s", 1L)), "nothing matches after clear");

        boolean threw = false;
        try { f.enableType(null); } catch (IllegalArgumentException ex) { threw = true; }
        check(threw, "enableType(null) throws IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
