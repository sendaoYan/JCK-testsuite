/*
 * @test
 * @summary jdk.internal.net.http.quic.streams.QuicStreamWriter conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.streams:+open
 * @run main QuicStreamWriterTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicStreamWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.streams.QuicStreamWriter", false, QuicStreamWriterTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.streams.QuicStreamWriter"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("connected"), "method:connected");
        check(methods.contains("credit"), "method:credit");
        check(methods.contains("queueForWriting"), "method:queueForWriting");
        check(methods.contains("reset"), "method:reset");
        check(methods.contains("scheduleForWriting"), "method:scheduleForWriting");
        check(methods.contains("sendingState"), "method:sendingState");
        check(methods.contains("stopSendingReceived"), "method:stopSendingReceived");
        check(methods.contains("stream"), "method:stream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
