/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicPacketReceiver conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicPacketReceiverTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicPacketReceiverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicPacketReceiver", false, QuicPacketReceiverTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicPacketReceiver"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("accepts"), "method:accepts");
        check(methods.contains("activeResetTokens"), "method:activeResetTokens");
        check(methods.contains("connectionIds"), "method:connectionIds");
        check(methods.contains("datagramDiscarded"), "method:datagramDiscarded");
        check(methods.contains("datagramDropped"), "method:datagramDropped");
        check(methods.contains("datagramSent"), "method:datagramSent");
        check(methods.contains("initialConnectionId"), "method:initialConnectionId");
        check(methods.contains("onWriteError"), "method:onWriteError");
        check(methods.contains("processIncoming"), "method:processIncoming");
        check(methods.contains("processStatelessReset"), "method:processStatelessReset");
        check(methods.contains("shutdown"), "method:shutdown");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
