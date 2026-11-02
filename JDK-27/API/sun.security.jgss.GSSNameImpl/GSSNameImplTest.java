/*
 * @test
 * @summary sun.security.jgss.GSSNameImpl conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss:+open
 * @run main GSSNameImplTest
 */
import java.lang.reflect.*;
import java.util.*;
public class GSSNameImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.GSSNameImpl", false, GSSNameImplTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.GSSNameImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("org.ietf.jgss.GSSName"), "iface:org.ietf.jgss.GSSName");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("canonicalize"), "method:canonicalize");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("export"), "method:export");
        check(methods.contains("getElement"), "method:getElement");
        check(methods.contains("getStringNameType"), "method:getStringNameType");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("isAnonymous"), "method:isAnonymous");
        check(methods.contains("isMN"), "method:isMN");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
