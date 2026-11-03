/*
 * @test
 * @summary com.sun.nio.sctp.NotificationHandler conformance: white-box structure + black-box behavior.
 * @run main NotificationHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;

public class NotificationHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.nio.sctp.NotificationHandler");
        check(c.isInterface(), "interface");
        check(c.getTypeParameters().length == 1, "one type parameter (generic)");
        Method m = c.getDeclaredMethod("handleNotification",
                com.sun.nio.sctp.Notification.class, Object.class);
        check(m.getReturnType().getName().equals("com.sun.nio.sctp.HandlerResult"), "returns HandlerResult");
        check(Modifier.isAbstract(m.getModifiers()), "handleNotification is abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.nio.sctp.Notification n = new com.sun.nio.sctp.Notification() {
            public com.sun.nio.sctp.Association association() { return null; }
        };

        // handler returning CONTINUE
        com.sun.nio.sctp.NotificationHandler<String> hContinue =
                (notification, attachment) -> com.sun.nio.sctp.HandlerResult.CONTINUE;
        check(hContinue.handleNotification(n, "att") == com.sun.nio.sctp.HandlerResult.CONTINUE, "CONTINUE handler");

        // handler returning RETURN
        com.sun.nio.sctp.NotificationHandler<Integer> hReturn =
                (notification, attachment) -> com.sun.nio.sctp.HandlerResult.RETURN;
        check(hReturn.handleNotification(n, 42) == com.sun.nio.sctp.HandlerResult.RETURN, "RETURN handler");

        // null notification / null attachment may still be handled
        check(hContinue.handleNotification(null, null) == com.sun.nio.sctp.HandlerResult.CONTINUE,
              "CONTINUE with null args");

        // capture the attachment to verify pass-through
        final String[] seen = new String[1];
        com.sun.nio.sctp.NotificationHandler<String> capture = (notification, attachment) -> {
            seen[0] = attachment;
            return com.sun.nio.sctp.HandlerResult.CONTINUE;
        };
        check(capture.handleNotification(n, "payload") == com.sun.nio.sctp.HandlerResult.CONTINUE, "capture returns CONTINUE");
        check("payload".equals(seen[0]), "attachment passed through");

        check(hContinue instanceof com.sun.nio.sctp.NotificationHandler, "hContinue instanceof NotificationHandler");
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
