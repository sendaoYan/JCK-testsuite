/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.implementations.SignatureBaseRSA.SignatureRSASHA224MGF1 conformance: class hierarchy and members.
 * @run main SignatureRSASHA224MGF1Test
 */
public class SignatureRSASHA224MGF1Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.implementations.SignatureBaseRSA$SignatureRSASHA224MGF1", false, SignatureRSASHA224MGF1Test.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.algorithms.implementations.SignatureBaseRSA"), "superclass com.sun.org.apache.xml.internal.security.algorithms.implementations.SignatureBaseRSA");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("engineGetURI"), "method engineGetURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
