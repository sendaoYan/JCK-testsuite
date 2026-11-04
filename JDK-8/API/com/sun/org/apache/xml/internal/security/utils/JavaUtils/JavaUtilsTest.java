/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.JavaUtils conformance: class hierarchy and members.
 * @run main JavaUtilsTest
 */
public class JavaUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.JavaUtils", false, JavaUtilsTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBytesFromFile"), "method getBytesFromFile");
        check(mn.contains("writeBytesToFilename"), "method writeBytesToFilename");
        check(mn.contains("getBytesFromStream"), "method getBytesFromStream");
        check(mn.contains("convertDsaASN1toXMLDSIG"), "method convertDsaASN1toXMLDSIG");
        check(mn.contains("convertDsaXMLDSIGtoASN1"), "method convertDsaXMLDSIGtoASN1");
        check(mn.contains("checkRegisterPermission"), "method checkRegisterPermission");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
