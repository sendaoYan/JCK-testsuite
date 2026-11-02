/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.JCEMapper conformance: class hierarchy and members.
 * @run main JCEMapperTest
 */
public class JCEMapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.JCEMapper", false, JCEMapperTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("register"), "method register");
        check(mn.contains("registerDefaultAlgorithms"), "method registerDefaultAlgorithms");
        check(mn.contains("translateURItoJCEID"), "method translateURItoJCEID");
        check(mn.contains("getAlgorithmClassFromURI"), "method getAlgorithmClassFromURI");
        check(mn.contains("getKeyLengthFromURI"), "method getKeyLengthFromURI");
        check(mn.contains("getIVLengthFromURI"), "method getIVLengthFromURI");
        check(mn.contains("getJCEKeyAlgorithmFromURI"), "method getJCEKeyAlgorithmFromURI");
        check(mn.contains("getJCEProviderFromURI"), "method getJCEProviderFromURI");
        check(mn.contains("getProviderId"), "method getProviderId");
        check(mn.contains("setProviderId"), "method setProviderId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
