/*
 * @test
 * @summary jdk.internal.net.http.common.TimeSource conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main TimeSourceTest
 */
import java.lang.reflect.*;
import java.util.*;
public class TimeSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.TimeSource", false, TimeSourceTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.TimeSource"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.common.TimeLine"), "iface:jdk.internal.net.http.common.TimeLine");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("instant"), "method:instant");
        check(methods.contains("now"), "method:now");
        check(methods.contains("source"), "method:source");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
