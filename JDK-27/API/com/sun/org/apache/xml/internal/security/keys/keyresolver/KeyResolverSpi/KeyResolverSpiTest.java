/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverSpi conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.keys.keyresolver:+open
 * @run main KeyResolverSpiTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KeyResolverSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverSpi", false, KeyResolverSpiTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverSpi"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("engineCanResolve"), "method:engineCanResolve");
        check(methods.contains("engineLookupAndResolvePrivateKey"), "method:engineLookupAndResolvePrivateKey");
        check(methods.contains("engineLookupAndResolvePublicKey"), "method:engineLookupAndResolvePublicKey");
        check(methods.contains("engineLookupAndResolveSecretKey"), "method:engineLookupAndResolveSecretKey");
        check(methods.contains("engineLookupResolveX509Certificate"), "method:engineLookupResolveX509Certificate");
        check(methods.contains("engineResolvePrivateKey"), "method:engineResolvePrivateKey");
        check(methods.contains("engineResolvePublicKey"), "method:engineResolvePublicKey");
        check(methods.contains("engineResolveSecretKey"), "method:engineResolveSecretKey");
        check(methods.contains("engineResolveX509Certificate"), "method:engineResolveX509Certificate");
        check(methods.contains("getDocFromBytes"), "method:getDocFromBytes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
