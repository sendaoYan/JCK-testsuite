/*
 * @test
 * @summary jdk.internal.net.http.HttpRequestImpl conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main HttpRequestImplTest
 */
import java.lang.reflect.*;
import java.util.*;
public class HttpRequestImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.HttpRequestImpl", false, HttpRequestImplTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.HttpRequestImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.net.http.HttpRequest"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.websocket.WebSocketRequest"), "iface:jdk.internal.net.http.websocket.WebSocketRequest");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("bodyPublisher"), "method:bodyPublisher");
        check(methods.contains("expectContinue"), "method:expectContinue");
        check(methods.contains("getOption"), "method:getOption");
        check(methods.contains("getUserSetAuthFlag"), "method:getUserSetAuthFlag");
        check(methods.contains("headers"), "method:headers");
        check(methods.contains("isWebSocket"), "method:isWebSocket");
        check(methods.contains("method"), "method:method");
        check(methods.contains("newInstanceForAuthentication"), "method:newInstanceForAuthentication");
        check(methods.contains("newInstanceForRedirection"), "method:newInstanceForRedirection");
        check(methods.contains("setProxy"), "method:setProxy");
        check(methods.contains("setSystemHeader"), "method:setSystemHeader");
        check(methods.contains("setUserSetAuthFlag"), "method:setUserSetAuthFlag");
        check(methods.contains("timeout"), "method:timeout");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("uri"), "method:uri");
        check(methods.contains("version"), "method:version");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("USER_AGENT"), "field:USER_AGENT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
