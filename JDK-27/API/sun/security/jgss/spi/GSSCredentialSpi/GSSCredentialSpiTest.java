/*
 * @test
 * @summary sun.security.jgss.spi.GSSCredentialSpi conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss.spi:+open
 * @run main GSSCredentialSpiTest
 */
import java.lang.reflect.*;
import java.util.*;
public class GSSCredentialSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.spi.GSSCredentialSpi", false, GSSCredentialSpiTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.spi.GSSCredentialSpi"), "name");
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
        check(methods.contains("dispose"), "method:dispose");
        check(methods.contains("getAcceptLifetime"), "method:getAcceptLifetime");
        check(methods.contains("getInitLifetime"), "method:getInitLifetime");
        check(methods.contains("getMechanism"), "method:getMechanism");
        check(methods.contains("getName"), "method:getName");
        check(methods.contains("getProvider"), "method:getProvider");
        check(methods.contains("impersonate"), "method:impersonate");
        check(methods.contains("isAcceptorCredential"), "method:isAcceptorCredential");
        check(methods.contains("isInitiatorCredential"), "method:isInitiatorCredential");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
