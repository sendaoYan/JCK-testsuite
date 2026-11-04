/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.MessageDigestAlgorithm conformance: class hierarchy and members.
 * @run main MessageDigestAlgorithmTest
 */
public class MessageDigestAlgorithmTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.MessageDigestAlgorithm", false, MessageDigestAlgorithmTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.algorithms.Algorithm"), "superclass com.sun.org.apache.xml.internal.security.algorithms.Algorithm");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getInstance"), "method getInstance");
        check(mn.contains("getAlgorithm"), "method getAlgorithm");
        check(mn.contains("isEqual"), "method isEqual");
        check(mn.contains("digest"), "method digest");
        check(mn.contains("getJCEAlgorithmString"), "method getJCEAlgorithmString");
        check(mn.contains("getJCEProvider"), "method getJCEProvider");
        check(mn.contains("getDigestLength"), "method getDigestLength");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("update"), "method update");
        check(mn.contains("getBaseNamespace"), "method getBaseNamespace");
        check(mn.contains("getBaseLocalName"), "method getBaseLocalName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
