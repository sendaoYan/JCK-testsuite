/*
 * @test
 * @summary sun.security.jgss.wrapper.GSSCredElement conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss.wrapper:+open
 * @run main GSSCredElementTest
 */
import java.lang.reflect.*;
import java.util.*;
public class GSSCredElementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.wrapper.GSSCredElement", false, GSSCredElementTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.wrapper.GSSCredElement"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("sun.security.jgss.spi.GSSCredentialSpi"), "iface:sun.security.jgss.spi.GSSCredentialSpi");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("dispose"), "method:dispose");
        check(methods.contains("getAcceptLifetime"), "method:getAcceptLifetime");
        check(methods.contains("getInitLifetime"), "method:getInitLifetime");
        check(methods.contains("getMechanism"), "method:getMechanism");
        check(methods.contains("getName"), "method:getName");
        check(methods.contains("getProvider"), "method:getProvider");
        check(methods.contains("impersonate"), "method:impersonate");
        check(methods.contains("isAcceptorCredential"), "method:isAcceptorCredential");
        check(methods.contains("isInitiatorCredential"), "method:isInitiatorCredential");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
