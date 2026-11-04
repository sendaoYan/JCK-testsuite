/*
 * @test
 * @summary jdk.internal.net.http.ResponseBodyHandlers.FileDownloadBodyHandler conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main FileDownloadBodyHandlerTest
 */
import java.lang.reflect.*;
import java.util.*;
public class FileDownloadBodyHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.ResponseBodyHandlers$FileDownloadBodyHandler", false, FileDownloadBodyHandlerTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.ResponseBodyHandlers$FileDownloadBodyHandler"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.net.http.HttpResponse$BodyHandler"), "iface:java.net.http.HttpResponse$BodyHandler");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("apply"), "method:apply");
        check(methods.contains("create"), "method:create");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
