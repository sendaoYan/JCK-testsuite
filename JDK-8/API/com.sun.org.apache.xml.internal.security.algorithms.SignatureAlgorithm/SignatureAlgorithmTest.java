/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.SignatureAlgorithm conformance: class hierarchy and members.
 * @run main SignatureAlgorithmTest
 */
public class SignatureAlgorithmTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.SignatureAlgorithm", false, SignatureAlgorithmTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.algorithms.Algorithm"), "superclass com.sun.org.apache.xml.internal.security.algorithms.Algorithm");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("sign"), "method sign");
        check(mn.contains("getJCEAlgorithmString"), "method getJCEAlgorithmString");
        check(mn.contains("getJCEProviderName"), "method getJCEProviderName");
        check(mn.contains("update"), "method update");
        check(mn.contains("initSign"), "method initSign");
        check(mn.contains("setParameter"), "method setParameter");
        check(mn.contains("initVerify"), "method initVerify");
        check(mn.contains("verify"), "method verify");
        check(mn.contains("getURI"), "method getURI");
        check(mn.contains("register"), "method register");
        check(mn.contains("registerDefaultAlgorithms"), "method registerDefaultAlgorithms");
        check(mn.contains("getBaseNamespace"), "method getBaseNamespace");
        check(mn.contains("getBaseLocalName"), "method getBaseLocalName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
