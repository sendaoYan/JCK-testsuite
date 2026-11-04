/*
 * @test
 * @summary jdk.internal.net.http.common.SSLFlowDelegate conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main SSLFlowDelegateTest
 */
import java.lang.reflect.*;
import java.util.*;
public class SSLFlowDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.SSLFlowDelegate", false, SSLFlowDelegateTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.SSLFlowDelegate"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("alpn"), "method:alpn");
        check(methods.contains("checkForHandshake"), "method:checkForHandshake");
        check(methods.contains("closeNotifyReceived"), "method:closeNotifyReceived");
        check(methods.contains("enterReadScheduling"), "method:enterReadScheduling");
        check(methods.contains("monitor"), "method:monitor");
        check(methods.contains("resetReaderDemand"), "method:resetReaderDemand");
        check(methods.contains("resumeReader"), "method:resumeReader");
        check(methods.contains("upstreamReader"), "method:upstreamReader");
        check(methods.contains("upstreamWriter"), "method:upstreamWriter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
