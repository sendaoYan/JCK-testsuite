/*
 * @test
 * @summary jdk.internal.net.http.HttpRequestBuilderImpl conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main HttpRequestBuilderImplTest
 */
import java.lang.reflect.*;
import java.util.*;
public class HttpRequestBuilderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.HttpRequestBuilderImpl", false, HttpRequestBuilderImplTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.HttpRequestBuilderImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.net.http.HttpRequest$Builder"), "iface:java.net.http.HttpRequest$Builder");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("DELETE"), "method:DELETE");
        check(methods.contains("GET"), "method:GET");
        check(methods.contains("HEAD"), "method:HEAD");
        check(methods.contains("POST"), "method:POST");
        check(methods.contains("PUT"), "method:PUT");
        check(methods.contains("build"), "method:build");
        check(methods.contains("buildForWebSocket"), "method:buildForWebSocket");
        check(methods.contains("copy"), "method:copy");
        check(methods.contains("copySupportedOptions"), "method:copySupportedOptions");
        check(methods.contains("expectContinue"), "method:expectContinue");
        check(methods.contains("header"), "method:header");
        check(methods.contains("headers"), "method:headers");
        check(methods.contains("method"), "method:method");
        check(methods.contains("setHeader"), "method:setHeader");
        check(methods.contains("setOption"), "method:setOption");
        check(methods.contains("timeout"), "method:timeout");
        check(methods.contains("uri"), "method:uri");
        check(methods.contains("version"), "method:version");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
