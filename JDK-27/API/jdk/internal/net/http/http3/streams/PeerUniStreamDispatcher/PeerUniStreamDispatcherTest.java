/*
 * @test
 * @summary jdk.internal.net.http.http3.streams.PeerUniStreamDispatcher conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.http3.streams:+open
 * @run main PeerUniStreamDispatcherTest
 */
import java.lang.reflect.*;
import java.util.*;
public class PeerUniStreamDispatcherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.http3.streams.PeerUniStreamDispatcher", false, PeerUniStreamDispatcherTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.http3.streams.PeerUniStreamDispatcher"), "name");
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
        check(methods.contains("abandon"), "method:abandon");
        check(methods.contains("debug"), "method:debug");
        check(methods.contains("dispatchCF"), "method:dispatchCF");
        check(methods.contains("onControlStreamCreated"), "method:onControlStreamCreated");
        check(methods.contains("onDecoderStreamCreated"), "method:onDecoderStreamCreated");
        check(methods.contains("onEncoderStreamCreated"), "method:onEncoderStreamCreated");
        check(methods.contains("onPushStreamCreated"), "method:onPushStreamCreated");
        check(methods.contains("onReservedStreamType"), "method:onReservedStreamType");
        check(methods.contains("onStreamAbandoned"), "method:onStreamAbandoned");
        check(methods.contains("onUnknownStreamType"), "method:onUnknownStreamType");
        check(methods.contains("start"), "method:start");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
