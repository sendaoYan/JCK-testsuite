/*
 * @test
 * @summary sun.security.jgss.GSSCredentialImpl conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss:+open
 * @run main GSSCredentialImplTest
 */
import java.lang.reflect.*;
import java.util.*;
public class GSSCredentialImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.GSSCredentialImpl", false, GSSCredentialImplTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.GSSCredentialImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("org.ietf.jgss.GSSCredential"), "iface:org.ietf.jgss.GSSCredential");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("add"), "method:add");
        check(methods.contains("dispose"), "method:dispose");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getElement"), "method:getElement");
        check(methods.contains("getMechs"), "method:getMechs");
        check(methods.contains("getName"), "method:getName");
        check(methods.contains("getRemainingAcceptLifetime"), "method:getRemainingAcceptLifetime");
        check(methods.contains("getRemainingInitLifetime"), "method:getRemainingInitLifetime");
        check(methods.contains("getRemainingLifetime"), "method:getRemainingLifetime");
        check(methods.contains("getUsage"), "method:getUsage");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("impersonate"), "method:impersonate");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
