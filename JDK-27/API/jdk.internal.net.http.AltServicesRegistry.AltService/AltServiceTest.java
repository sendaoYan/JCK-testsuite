/*
 * @test
 * @summary jdk.internal.net.http.AltServicesRegistry.AltService conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main AltServiceTest
 */
import java.lang.reflect.*;
import java.util.*;
public class AltServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.AltServicesRegistry$AltService", false, AltServiceTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.AltServicesRegistry$AltService"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("alpn"), "method:alpn");
        check(methods.contains("authority"), "method:authority");
        check(methods.contains("create"), "method:create");
        check(methods.contains("deadline"), "method:deadline");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("host"), "method:host");
        check(methods.contains("identity"), "method:identity");
        check(methods.contains("isPersist"), "method:isPersist");
        check(methods.contains("origin"), "method:origin");
        check(methods.contains("originHasSameAuthority"), "method:originHasSameAuthority");
        check(methods.contains("port"), "method:port");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("wasAdvertised"), "method:wasAdvertised");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
