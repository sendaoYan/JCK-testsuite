/*
 * @test
 * @summary jdk.internal.net.http.HttpClientFacade conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main HttpClientFacadeTest
 */
import java.lang.reflect.*;
import java.util.*;
public class HttpClientFacadeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.HttpClientFacade", false, HttpClientFacadeTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.HttpClientFacade"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.net.http.HttpClient"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.common.OperationTrackers$Trackable"), "iface:jdk.internal.net.http.common.OperationTrackers$Trackable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("authenticator"), "method:authenticator");
        check(methods.contains("awaitTermination"), "method:awaitTermination");
        check(methods.contains("close"), "method:close");
        check(methods.contains("connectTimeout"), "method:connectTimeout");
        check(methods.contains("cookieHandler"), "method:cookieHandler");
        check(methods.contains("executor"), "method:executor");
        check(methods.contains("followRedirects"), "method:followRedirects");
        check(methods.contains("getOperationsTracker"), "method:getOperationsTracker");
        check(methods.contains("isTerminated"), "method:isTerminated");
        check(methods.contains("newWebSocketBuilder"), "method:newWebSocketBuilder");
        check(methods.contains("proxy"), "method:proxy");
        check(methods.contains("send"), "method:send");
        check(methods.contains("sendAsync"), "method:sendAsync");
        check(methods.contains("shutdown"), "method:shutdown");
        check(methods.contains("shutdownNow"), "method:shutdownNow");
        check(methods.contains("sslContext"), "method:sslContext");
        check(methods.contains("sslParameters"), "method:sslParameters");
        check(methods.contains("theExecutor"), "method:theExecutor");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("version"), "method:version");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
