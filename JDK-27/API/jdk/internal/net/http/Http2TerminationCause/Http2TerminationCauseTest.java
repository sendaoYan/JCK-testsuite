/*
 * @test
 * @summary jdk.internal.net.http.Http2TerminationCause conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main Http2TerminationCauseTest
 */
import java.lang.reflect.*;
import java.util.*;
public class Http2TerminationCauseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.Http2TerminationCause", false, Http2TerminationCauseTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.Http2TerminationCause"), "name");
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
        check(methods.contains("forException"), "method:forException");
        check(methods.contains("forH2Error"), "method:forH2Error");
        check(methods.contains("getCloseCause"), "method:getCloseCause");
        check(methods.contains("getCloseCode"), "method:getCloseCode");
        check(methods.contains("getLogMsg"), "method:getLogMsg");
        check(methods.contains("getPeerVisibleReason"), "method:getPeerVisibleReason");
        check(methods.contains("idleTimedOut"), "method:idleTimedOut");
        check(methods.contains("isAbnormalClose"), "method:isAbnormalClose");
        check(methods.contains("noErrorTermination"), "method:noErrorTermination");
        check(methods.contains("setPeerVisibleReason"), "method:setPeerVisibleReason");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
