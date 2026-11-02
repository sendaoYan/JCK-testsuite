/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.implementations.SignatureDSA conformance: class hierarchy and members.
 * @run main SignatureDSATest
 */
public class SignatureDSATest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.implementations.SignatureDSA", false, SignatureDSATest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.algorithms.SignatureAlgorithmSpi"), "superclass com.sun.org.apache.xml.internal.security.algorithms.SignatureAlgorithmSpi");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("engineGetURI"), "method engineGetURI");
        check(mn.contains("engineSetParameter"), "method engineSetParameter");
        check(mn.contains("engineVerify"), "method engineVerify");
        check(mn.contains("engineInitVerify"), "method engineInitVerify");
        check(mn.contains("engineSign"), "method engineSign");
        check(mn.contains("engineInitSign"), "method engineInitSign");
        check(mn.contains("engineUpdate"), "method engineUpdate");
        check(mn.contains("engineGetJCEAlgorithmString"), "method engineGetJCEAlgorithmString");
        check(mn.contains("engineGetJCEProviderName"), "method engineGetJCEProviderName");
        check(mn.contains("engineSetHMACOutputLength"), "method engineSetHMACOutputLength");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
