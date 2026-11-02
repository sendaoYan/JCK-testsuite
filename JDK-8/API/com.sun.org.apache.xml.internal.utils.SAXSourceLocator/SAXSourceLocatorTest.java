/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.SAXSourceLocator conformance: class hierarchy and members.
 * @run main SAXSourceLocatorTest
 */
public class SAXSourceLocatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.SAXSourceLocator", false, SAXSourceLocatorTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("org.xml.sax.helpers.LocatorImpl"), "superclass org.xml.sax.helpers.LocatorImpl");
        check(hasInterface(c, "javax.xml.transform.SourceLocator"), "has interface javax.xml.transform.SourceLocator");
        check(hasInterface(c, "java.io.Serializable"), "has interface java.io.Serializable");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getPublicId"), "method getPublicId");
        check(mn.contains("getSystemId"), "method getSystemId");
        check(mn.contains("getLineNumber"), "method getLineNumber");
        check(mn.contains("getColumnNumber"), "method getColumnNumber");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
