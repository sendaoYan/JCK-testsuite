/*
 * @test
 * @summary com.sun.nio.sctp.SendFailedNotification conformance: white-box structure + black-box behavior.
 * @run main SendFailedNotificationTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.nio.*;
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.nio.sctp.SendFailedNotification");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(interfaces(c).contains("com.sun.nio.sctp.Notification"), "implements Notification");
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(Modifier.isProtected(ctor.getModifiers()), "protected no-arg constructor");

        Set<String> m = methodNames(c);
        check(m.contains("association"), "association");
        check(m.contains("address"), "address");
        check(m.contains("buffer"), "buffer");
        check(m.contains("errorCode"), "errorCode");
        check(m.contains("streamNumber"), "streamNumber");

        check(c.getDeclaredMethod("buffer").getReturnType() == java.nio.ByteBuffer.class, "buffer returns ByteBuffer");
        check(c.getDeclaredMethod("errorCode").getReturnType() == int.class, "errorCode returns int");
        check(c.getDeclaredMethod("streamNumber").getReturnType() == int.class, "streamNumber returns int");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SocketAddress addr = new InetSocketAddress("127.0.0.1", 12345);
        com.sun.nio.sctp.Association assoc = new com.sun.nio.sctp.Association(1, 2, 3) {};
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{1, 2, 3, 4});

        com.sun.nio.sctp.SendFailedNotification n =
                new com.sun.nio.sctp.SendFailedNotification() {
                    public com.sun.nio.sctp.Association association() { return assoc; }
                    public SocketAddress address() { return addr; }
                    public ByteBuffer buffer() { return buffer; }
                    public int errorCode() { return 7; }
                    public int streamNumber() { return 9; }
                };

        check(n.association() == assoc, "association returns");
        check(n.address() == addr, "address returns");
        check(n.buffer() == buffer, "buffer returns");
        check(n.errorCode() == 7, "errorCode returns");
        check(n.streamNumber() == 9, "streamNumber returns");
        check(n instanceof com.sun.nio.sctp.Notification, "instanceof Notification");

        // different values; negative error/stream values are just data, no validation
        com.sun.nio.sctp.SendFailedNotification n2 =
                new com.sun.nio.sctp.SendFailedNotification() {
                    public com.sun.nio.sctp.Association association() { return null; }
                    public SocketAddress address() { return null; }
                    public ByteBuffer buffer() { return null; }
                    public int errorCode() { return -1; }
                    public int streamNumber() { return -2; }
                };
        check(n2.association() == null, "null association allowed");
        check(n2.address() == null, "null address allowed");
        check(n2.buffer() == null, "null buffer allowed");
        check(n2.errorCode() == -1, "negative errorCode passed through");
        check(n2.streamNumber() == -2, "negative streamNumber passed through");
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
