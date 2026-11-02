/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.implementations.ECDSAUtils conformance: class hierarchy and members.
 * @run main ECDSAUtilsTest
 */
public class ECDSAUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.implementations.ECDSAUtils", false, ECDSAUtilsTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("convertASN1toXMLDSIG"), "method convertASN1toXMLDSIG");
        check(mn.contains("convertXMLDSIGtoASN1"), "method convertXMLDSIGtoASN1");
        check(mn.contains("getOIDFromPublicKey"), "method getOIDFromPublicKey");
        check(mn.contains("getECCurveDefinition"), "method getECCurveDefinition");
        check(mn.contains("encodePoint"), "method encodePoint");
        check(mn.contains("decodePoint"), "method decodePoint");
        check(mn.contains("stripLeadingZeros"), "method stripLeadingZeros");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
