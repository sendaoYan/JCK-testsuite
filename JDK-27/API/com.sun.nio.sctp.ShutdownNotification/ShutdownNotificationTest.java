/*
 * @test
 * @summary com.sun.nio.sctp.ShutdownNotification conformance: abstract notification.
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

    public static void main(String[] a) throws Exception {
        Class<?> c = load("com.sun.nio.sctp.ShutdownNotification");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(interfaces(c).contains("com.sun.nio.sctp.Notification"), "implements Notification");
        check(methodNames(c).contains("association"), "association");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
