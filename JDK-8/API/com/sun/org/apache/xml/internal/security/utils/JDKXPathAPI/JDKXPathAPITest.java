/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.JDKXPathAPI conformance: class hierarchy and members.
 * @run main JDKXPathAPITest
 */
public class JDKXPathAPITest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.JDKXPathAPI", false, JDKXPathAPITest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.security.utils.XPathAPI"), "has interface com.sun.org.apache.xml.internal.security.utils.XPathAPI");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("selectNodeList"), "method selectNodeList");
        check(mn.contains("evaluate"), "method evaluate");
        check(mn.contains("clear"), "method clear");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
