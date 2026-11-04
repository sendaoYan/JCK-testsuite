/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolver conformance: class hierarchy and members.
 * @run main KeyResolverTest
 */
public class KeyResolverTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolver", false, KeyResolverTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("length"), "method length");
        check(mn.contains("getX509Certificate"), "method getX509Certificate");
        check(mn.contains("getPublicKey"), "method getPublicKey");
        check(mn.contains("register"), "method register");
        check(mn.contains("registerAtStart"), "method registerAtStart");
        check(mn.contains("registerClassNames"), "method registerClassNames");
        check(mn.contains("registerDefaultResolvers"), "method registerDefaultResolvers");
        check(mn.contains("resolvePublicKey"), "method resolvePublicKey");
        check(mn.contains("resolveX509Certificate"), "method resolveX509Certificate");
        check(mn.contains("resolveSecretKey"), "method resolveSecretKey");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getProperty"), "method getProperty");
        check(mn.contains("understandsProperty"), "method understandsProperty");
        check(mn.contains("resolverClassName"), "method resolverClassName");
        check(mn.contains("iterator"), "method iterator");
        check(mn.contains("access$000"), "method access$000");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
