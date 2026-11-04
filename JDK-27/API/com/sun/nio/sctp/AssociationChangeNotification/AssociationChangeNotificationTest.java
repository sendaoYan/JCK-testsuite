/*
 * @test
 * @summary com.sun.nio.sctp.AssociationChangeNotification conformance: white-box structure + black-box behavior.
 * @run main AssociationChangeNotificationTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AssociationChangeNotificationTest {
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
        Class<?> c = load("com.sun.nio.sctp.AssociationChangeNotification");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(interfaces(c).contains("com.sun.nio.sctp.Notification"), "implements Notification");
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "protected no-arg constructor");

        Set<String> m = methodNames(c);
        check(m.contains("association"), "association");
        check(m.contains("event"), "event");
        Method ev = c.getDeclaredMethod("event");
        check(ev.getReturnType().getName().equals(
                "com.sun.nio.sctp.AssociationChangeNotification$AssocChangeEvent"), "event returns AssocChangeEvent");

        // nested enum declared inside this class
        check(c.getDeclaredClasses().length >= 1, "nested AssocChangeEvent declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.nio.sctp.Association assoc = new com.sun.nio.sctp.Association(1, 2, 3) {};
        com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent ev =
                com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent.COMM_UP;

        com.sun.nio.sctp.AssociationChangeNotification n =
                new com.sun.nio.sctp.AssociationChangeNotification() {
                    public com.sun.nio.sctp.Association association() { return assoc; }
                    public com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent event() { return ev; }
                };

        check(n.association() == assoc, "association returns");
        check(n.event() == ev, "event returns");
        check(n.association().associationID() == 1, "association id reachable");
        check(n instanceof com.sun.nio.sctp.Notification, "instanceof Notification");

        // AssocChangeEvent enum constants and round-trips
        com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent[] vals =
                com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent.values();
        check(vals.length == 5, "AssocChangeEvent has 5 constants");
        Set<String> names = new HashSet<>(Arrays.asList(
                "COMM_UP", "COMM_LOST", "RESTART", "SHUTDOWN", "CANT_START"));
        for (com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent e : vals) {
            check(names.contains(e.name()), "constant " + e.name());
        }
        for (String nm : names) {
            check(com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent.valueOf(nm).name().equals(nm),
                  "valueOf " + nm + " round-trip");
        }

        // an implementation can select any event and null association
        com.sun.nio.sctp.AssociationChangeNotification n2 =
                new com.sun.nio.sctp.AssociationChangeNotification() {
                    public com.sun.nio.sctp.Association association() { return null; }
                    public com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent event() {
                        return com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent.SHUTDOWN;
                    }
                };
        check(n2.association() == null, "null association allowed");
        check(n2.event() == com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent.SHUTDOWN,
              "SHUTDOWN event allowed");
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
