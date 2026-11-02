/*
 * @test
 * @summary com.sun.nio.sctp.AbstractNotificationHandler conformance: default handler results.
 * @run main AbstractNotificationHandlerTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AbstractNotificationHandlerTest {
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
        Class<?> c = load("com.sun.nio.sctp.AbstractNotificationHandler");
        check(!c.isInterface(), "not an interface");
        com.sun.nio.sctp.AbstractNotificationHandler<Object> h = new com.sun.nio.sctp.AbstractNotificationHandler<Object>() {};
        check(h.handleNotification((com.sun.nio.sctp.Notification)null, null) == com.sun.nio.sctp.HandlerResult.CONTINUE, "generic CONTINUE");
        check(h.handleNotification((com.sun.nio.sctp.AssociationChangeNotification)null, null) == com.sun.nio.sctp.HandlerResult.CONTINUE, "assoc CONTINUE");
        check(h.handleNotification((com.sun.nio.sctp.PeerAddressChangeNotification)null, null) == com.sun.nio.sctp.HandlerResult.CONTINUE, "peer CONTINUE");
        check(h.handleNotification((com.sun.nio.sctp.SendFailedNotification)null, null) == com.sun.nio.sctp.HandlerResult.CONTINUE, "sendfail CONTINUE");
        check(h.handleNotification((com.sun.nio.sctp.ShutdownNotification)null, null) == com.sun.nio.sctp.HandlerResult.CONTINUE, "shutdown CONTINUE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
