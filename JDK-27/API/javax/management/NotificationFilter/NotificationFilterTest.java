/*
 * @test
 * @summary javax.management.NotificationFilter conformance: white-box structure + light behavior.
 * @run main NotificationFilterTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class NotificationFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.NotificationFilter");
        check(c.isInterface(), "is interface");
        check(java.io.Serializable.class.isAssignableFrom(c), "extends Serializable");
        check(hasMethod(c, "isNotificationEnabled"), "has isNotificationEnabled");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final boolean[] called = { false };
        NotificationFilter filter = new NotificationFilter() {
            public boolean isNotificationEnabled(Notification n) {
                called[0] = true;
                return "yes".equals(n.getType());
            }
        };
        check(filter.isNotificationEnabled(new Notification("yes", "s", 1L)), "filter enables matching");
        check(!filter.isNotificationEnabled(new Notification("no", "s", 1L)), "filter rejects non-matching");
        check(called[0], "filter invoked");

        // built-in implementations are NotificationFilters
        check(new NotificationFilterSupport() instanceof NotificationFilter, "NotificationFilterSupport is a NotificationFilter");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
