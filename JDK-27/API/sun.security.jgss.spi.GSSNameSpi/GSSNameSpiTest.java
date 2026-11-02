/*
 * @test
 * @summary sun.security.jgss.spi.GSSNameSpi conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss.spi:+open
 * @run main GSSNameSpiTest
 */
import java.lang.reflect.*;
import java.util.*;
public class GSSNameSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.spi.GSSNameSpi", false, GSSNameSpiTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.spi.GSSNameSpi"), "name");
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
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("export"), "method:export");
        check(methods.contains("getMechanism"), "method:getMechanism");
        check(methods.contains("getProvider"), "method:getProvider");
        check(methods.contains("getStringNameType"), "method:getStringNameType");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("isAnonymousName"), "method:isAnonymousName");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
