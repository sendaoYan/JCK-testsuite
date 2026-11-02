/*
 * @test
 * @summary jdk.internal.net.http.common.OperationTrackers.Tracker conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main TrackerTest
 */
import java.lang.reflect.*;
import java.util.*;
public class TrackerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.OperationTrackers$Tracker", false, TrackerTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.OperationTrackers$Tracker"), "name");
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
        check(methods.contains("getName"), "method:getName");
        check(methods.contains("getOutstandingHttp2Streams"), "method:getOutstandingHttp2Streams");
        check(methods.contains("getOutstandingHttp3Streams"), "method:getOutstandingHttp3Streams");
        check(methods.contains("getOutstandingHttpOperations"), "method:getOutstandingHttpOperations");
        check(methods.contains("getOutstandingHttpRequests"), "method:getOutstandingHttpRequests");
        check(methods.contains("getOutstandingOperations"), "method:getOutstandingOperations");
        check(methods.contains("getOutstandingSubscribers"), "method:getOutstandingSubscribers");
        check(methods.contains("getOutstandingTcpConnections"), "method:getOutstandingTcpConnections");
        check(methods.contains("getOutstandingWebSocketOperations"), "method:getOutstandingWebSocketOperations");
        check(methods.contains("isFacadeReferenced"), "method:isFacadeReferenced");
        check(methods.contains("isImplementationReferenced"), "method:isImplementationReferenced");
        check(methods.contains("isSelectorAlive"), "method:isSelectorAlive");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
