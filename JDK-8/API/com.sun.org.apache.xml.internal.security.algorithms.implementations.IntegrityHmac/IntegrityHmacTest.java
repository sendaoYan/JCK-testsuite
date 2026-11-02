/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.implementations.IntegrityHmac conformance: abstract class hierarchy and members.
 * @run main IntegrityHmacTest
 */
public class IntegrityHmacTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.implementations.IntegrityHmac", false, IntegrityHmacTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.algorithms.SignatureAlgorithmSpi"), "superclass com.sun.org.apache.xml.internal.security.algorithms.SignatureAlgorithmSpi");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("engineGetURI"), "method engineGetURI");
        check(mn.contains("getDigestLength"), "method getDigestLength");
        check(mn.contains("engineSetParameter"), "method engineSetParameter");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("engineVerify"), "method engineVerify");
        check(mn.contains("engineInitVerify"), "method engineInitVerify");
        check(mn.contains("engineSign"), "method engineSign");
        check(mn.contains("engineInitSign"), "method engineInitSign");
        check(mn.contains("engineUpdate"), "method engineUpdate");
        check(mn.contains("engineGetJCEAlgorithmString"), "method engineGetJCEAlgorithmString");
        check(mn.contains("engineGetJCEProviderName"), "method engineGetJCEProviderName");
        check(mn.contains("engineSetHMACOutputLength"), "method engineSetHMACOutputLength");
        check(mn.contains("engineGetContextFromElement"), "method engineGetContextFromElement");
        check(mn.contains("engineAddContextToElement"), "method engineAddContextToElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
