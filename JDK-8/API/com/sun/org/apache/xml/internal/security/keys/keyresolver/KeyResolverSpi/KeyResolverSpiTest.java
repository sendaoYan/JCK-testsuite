/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverSpi conformance: abstract class hierarchy and members.
 * @run main KeyResolverSpiTest
 */
public class KeyResolverSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverSpi", false, KeyResolverSpiTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setSecureValidation"), "method setSecureValidation");
        check(mn.contains("engineCanResolve"), "method engineCanResolve");
        check(mn.contains("engineResolvePublicKey"), "method engineResolvePublicKey");
        check(mn.contains("engineLookupAndResolvePublicKey"), "method engineLookupAndResolvePublicKey");
        check(mn.contains("engineResolveX509Certificate"), "method engineResolveX509Certificate");
        check(mn.contains("engineLookupResolveX509Certificate"), "method engineLookupResolveX509Certificate");
        check(mn.contains("engineResolveSecretKey"), "method engineResolveSecretKey");
        check(mn.contains("engineLookupAndResolveSecretKey"), "method engineLookupAndResolveSecretKey");
        check(mn.contains("engineLookupAndResolvePrivateKey"), "method engineLookupAndResolvePrivateKey");
        check(mn.contains("engineSetProperty"), "method engineSetProperty");
        check(mn.contains("engineGetProperty"), "method engineGetProperty");
        check(mn.contains("understandsProperty"), "method understandsProperty");
        check(mn.contains("setGlobalResolver"), "method setGlobalResolver");
        check(mn.contains("getDocFromBytes"), "method getDocFromBytes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
