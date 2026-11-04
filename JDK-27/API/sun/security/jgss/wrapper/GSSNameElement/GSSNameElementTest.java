/*
 * @test
 * @summary sun.security.jgss.wrapper.GSSNameElement conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss.wrapper:+open
 * @run main GSSNameElementTest
 */
import java.lang.reflect.*;
import java.util.*;
public class GSSNameElementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.wrapper.GSSNameElement", false, GSSNameElementTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.wrapper.GSSNameElement"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("sun.security.jgss.spi.GSSNameSpi"), "iface:sun.security.jgss.spi.GSSNameSpi");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("dispose"), "method:dispose");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("export"), "method:export");
        check(methods.contains("getKrbName"), "method:getKrbName");
        check(methods.contains("getMechanism"), "method:getMechanism");
        check(methods.contains("getProvider"), "method:getProvider");
        check(methods.contains("getStringNameType"), "method:getStringNameType");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("isAnonymousName"), "method:isAnonymousName");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
