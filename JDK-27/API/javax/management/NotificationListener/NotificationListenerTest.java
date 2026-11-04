/*
 * @test
 * @summary javax.management.NotificationListener conformance: white-box structure + light behavior.
 * @run main NotificationListenerTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class NotificationListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.NotificationListener");
        check(c.isInterface(), "is interface");
        check(java.util.EventListener.class.isAssignableFrom(c), "extends EventListener");
        check(hasMethod(c, "handleNotification"), "has handleNotification");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final List<String> types = new ArrayList<String>();
        final List<Object> handbacks = new ArrayList<Object>();
        NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification n, Object handback) {
                types.add(n.getType());
                handbacks.add(handback);
            }
        };
        Notification n = new Notification("type", "src", 1L);
        listener.handleNotification(n, "hb");
        check(types.size() == 1 && "type".equals(types.get(0)), "handleNotification receives type");
        check("hb".equals(handbacks.get(0)), "handleNotification receives handback");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
