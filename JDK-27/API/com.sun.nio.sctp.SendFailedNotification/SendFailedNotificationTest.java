/*
 * @test
 * @summary com.sun.nio.sctp.SendFailedNotification conformance: abstract notification.
 * @run main SendFailedNotificationTest
 */

import java.lang.reflect.*;
import java.util.*;

public class SendFailedNotificationTest {
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
        Class<?> c = load("com.sun.nio.sctp.SendFailedNotification");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(interfaces(c).contains("com.sun.nio.sctp.Notification"), "implements Notification");
        Set<String> m = methodNames(c);
        check(m.contains("association"), "association");
        check(m.contains("address"), "address");
        check(m.contains("buffer"), "buffer");
        check(m.contains("errorCode"), "errorCode");
        check(m.contains("streamNumber"), "streamNumber");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
