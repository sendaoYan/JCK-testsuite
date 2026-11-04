/*
 * @test
 * @summary com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent conformance: enum constants.
 * @run main AssocChangeEventTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AssocChangeEventTest {
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

    public static void main(String[] a) throws Exception {
        com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent[] v = com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent.values();
        check(v.length == 5, "5 constants");
        check(com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent.COMM_UP.name().equals("COMM_UP"), "COMM_UP");
        check(com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent.valueOf("SHUTDOWN") == com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent.SHUTDOWN, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
