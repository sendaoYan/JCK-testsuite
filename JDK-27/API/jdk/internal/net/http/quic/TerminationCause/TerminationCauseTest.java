/*
 * @test
 * @summary jdk.internal.net.http.quic.TerminationCause conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main TerminationCauseTest
 */
import java.lang.reflect.*;
import java.util.*;
public class TerminationCauseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.TerminationCause", false, TerminationCauseTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.TerminationCause"), "name");
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
        check(methods.contains("appLayerClose"), "method:appLayerClose");
        check(methods.contains("appLayerException"), "method:appLayerException");
        check(methods.contains("forException"), "method:forException");
        check(methods.contains("forTransportError"), "method:forTransportError");
        check(methods.contains("getCloseCause"), "method:getCloseCause");
        check(methods.contains("getCloseCode"), "method:getCloseCode");
        check(methods.contains("getLogMsg"), "method:getLogMsg");
        check(methods.contains("getPeerVisibleReason"), "method:getPeerVisibleReason");
        check(methods.contains("isAppLayer"), "method:isAppLayer");
        check(methods.contains("loggedAs"), "method:loggedAs");
        check(methods.contains("peerVisibleReason"), "method:peerVisibleReason");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
