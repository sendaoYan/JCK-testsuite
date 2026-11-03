/*
 * @test
 * @summary com.sun.nio.sctp.AbstractNotificationHandler conformance: white-box structure + black-box behavior.
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.nio.sctp.AbstractNotificationHandler");
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getTypeParameters().length == 1, "one type parameter (generic)");
        check(interfaces(c).contains("com.sun.nio.sctp.NotificationHandler"), "implements NotificationHandler");
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "protected no-arg constructor");

        Set<String> m = methodNames(c);
        check(m.contains("handleNotification"), "handleNotification");
        // five overloads: generic + 4 concrete notification types
        int overloads = 0;
        for (Method mm : c.getDeclaredMethods()) {
            if (mm.getName().equals("handleNotification")) overloads++;
        }
        check(overloads == 5, "five handleNotification overloads");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.nio.sctp.AbstractNotificationHandler<Object> h =
                new com.sun.nio.sctp.AbstractNotificationHandler<Object>() {};

        // default implementation consumes all notifications and continues
        check(h.handleNotification((com.sun.nio.sctp.Notification) null, null)
                == com.sun.nio.sctp.HandlerResult.CONTINUE, "generic CONTINUE");
        check(h.handleNotification((com.sun.nio.sctp.AssociationChangeNotification) null, null)
                == com.sun.nio.sctp.HandlerResult.CONTINUE, "assoc CONTINUE");
        check(h.handleNotification((com.sun.nio.sctp.PeerAddressChangeNotification) null, null)
                == com.sun.nio.sctp.HandlerResult.CONTINUE, "peer CONTINUE");
        check(h.handleNotification((com.sun.nio.sctp.SendFailedNotification) null, null)
                == com.sun.nio.sctp.HandlerResult.CONTINUE, "sendfail CONTINUE");
        check(h.handleNotification((com.sun.nio.sctp.ShutdownNotification) null, null)
                == com.sun.nio.sctp.HandlerResult.CONTINUE, "shutdown CONTINUE");

        // non-null attachment is ignored, result still CONTINUE
        String attachment = "attachment";
        check(h.handleNotification((com.sun.nio.sctp.Notification) null, attachment)
                == com.sun.nio.sctp.HandlerResult.CONTINUE, "generic CONTINUE with attachment");

        // concrete Notification instance flows through the generic overload
        com.sun.nio.sctp.Notification n = new com.sun.nio.sctp.Notification() {
            public com.sun.nio.sctp.Association association() { return null; }
        };
        check(h.handleNotification(n, null) == com.sun.nio.sctp.HandlerResult.CONTINUE,
              "generic CONTINUE for concrete notification");

        // subclass may override to RETURN
        com.sun.nio.sctp.AbstractNotificationHandler<Object> override =
                new com.sun.nio.sctp.AbstractNotificationHandler<Object>() {
                    @Override
                    public com.sun.nio.sctp.HandlerResult handleNotification(
                            com.sun.nio.sctp.ShutdownNotification notification, Object attachment) {
                        return com.sun.nio.sctp.HandlerResult.RETURN;
                    }
                };
        check(override.handleNotification((com.sun.nio.sctp.ShutdownNotification) null, null)
                == com.sun.nio.sctp.HandlerResult.RETURN, "override shutdown -> RETURN");
        check(override.handleNotification((com.sun.nio.sctp.AssociationChangeNotification) null, null)
                == com.sun.nio.sctp.HandlerResult.CONTINUE, "override leaves assoc CONTINUE");
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
