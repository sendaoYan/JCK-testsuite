/*
 * @test
 * @summary com.sun.nio.sctp.ShutdownNotification conformance: white-box structure + black-box behavior.
 * @run main ShutdownNotificationTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ShutdownNotificationTest {
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
        Class<?> c = load("com.sun.nio.sctp.ShutdownNotification");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(interfaces(c).contains("com.sun.nio.sctp.Notification"), "implements Notification");
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "protected no-arg constructor");

        Set<String> m = methodNames(c);
        check(m.contains("association"), "association");
        check(c.getDeclaredMethod("association").getReturnType().getName().equals(
                "com.sun.nio.sctp.Association"), "association returns Association");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.nio.sctp.Association a1 = new com.sun.nio.sctp.Association(1, 2, 3) {};
        com.sun.nio.sctp.ShutdownNotification n = new com.sun.nio.sctp.ShutdownNotification() {
            public com.sun.nio.sctp.Association association() { return a1; }
        };
        check(n.association() == a1, "association returns");
        check(n.association().associationID() == 1, "association id reachable");
        check(n instanceof com.sun.nio.sctp.Notification, "instanceof Notification");

        com.sun.nio.sctp.ShutdownNotification n2 = new com.sun.nio.sctp.ShutdownNotification() {
            public com.sun.nio.sctp.Association association() { return null; }
        };
        check(n2.association() == null, "null association allowed");
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
