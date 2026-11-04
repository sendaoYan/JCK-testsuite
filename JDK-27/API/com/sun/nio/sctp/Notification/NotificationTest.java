/*
 * @test
 * @summary com.sun.nio.sctp.Notification conformance: white-box structure + black-box behavior.
 * @run main NotificationTest
 */

import java.lang.reflect.*;
import java.util.*;

public class NotificationTest {
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
        Class<?> c = load("com.sun.nio.sctp.Notification");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "no superclass (interface)");
        Method m = c.getDeclaredMethod("association");
        check(m.getReturnType().getName().equals("com.sun.nio.sctp.Association"), "association returns Association");
        check(Modifier.isAbstract(m.getModifiers()), "association is abstract");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.nio.sctp.Association a1 = new com.sun.nio.sctp.Association(1, 2, 3) {};
        com.sun.nio.sctp.Association a2 = new com.sun.nio.sctp.Association(9, 8, 7) {};

        com.sun.nio.sctp.Notification n1 = new com.sun.nio.sctp.Notification() {
            public com.sun.nio.sctp.Association association() { return a1; }
        };
        com.sun.nio.sctp.Notification n2 = new com.sun.nio.sctp.Notification() {
            public com.sun.nio.sctp.Association association() { return a2; }
        };

        check(n1.association() == a1, "n1 association");
        check(n2.association() == a2, "n2 association");
        check(n1.association().associationID() == 1, "n1 association id reachable");
        check(n2.association().maxOutboundStreams() == 7, "n2 association accessor reachable");

        // an implementation may legitimately return null
        com.sun.nio.sctp.Notification n3 = new com.sun.nio.sctp.Notification() {
            public com.sun.nio.sctp.Association association() { return null; }
        };
        check(n3.association() == null, "null association allowed");

        // instanceof check against the interface
        check(n1 instanceof com.sun.nio.sctp.Notification, "n1 instanceof Notification");
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
