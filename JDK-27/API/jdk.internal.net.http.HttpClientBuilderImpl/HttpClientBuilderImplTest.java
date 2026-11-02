/*
 * @test
 * @summary jdk.internal.net.http.HttpClientBuilderImpl conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main HttpClientBuilderImplTest
 */
import java.lang.reflect.*;
import java.util.*;
public class HttpClientBuilderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.HttpClientBuilderImpl", false, HttpClientBuilderImplTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.HttpClientBuilderImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.net.http.HttpClient$Builder"), "iface:java.net.http.HttpClient$Builder");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("authenticator"), "method:authenticator");
        check(methods.contains("build"), "method:build");
        check(methods.contains("connectTimeout"), "method:connectTimeout");
        check(methods.contains("cookieHandler"), "method:cookieHandler");
        check(methods.contains("executor"), "method:executor");
        check(methods.contains("followRedirects"), "method:followRedirects");
        check(methods.contains("localAddress"), "method:localAddress");
        check(methods.contains("priority"), "method:priority");
        check(methods.contains("proxy"), "method:proxy");
        check(methods.contains("sslContext"), "method:sslContext");
        check(methods.contains("sslParameters"), "method:sslParameters");
        check(methods.contains("version"), "method:version");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
