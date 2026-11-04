/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.transforms.params.XPathFilterCHGPContainer conformance: class hierarchy and members.
 * @run main XPathFilterCHGPContainerTest
 */
public class XPathFilterCHGPContainerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.transforms.params.XPathFilterCHGPContainer", false, XPathFilterCHGPContainerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.ElementProxy"), "superclass com.sun.org.apache.xml.internal.security.utils.ElementProxy");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.security.transforms.TransformParam"), "has interface com.sun.org.apache.xml.internal.security.transforms.TransformParam");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("indentXPathText"), "method indentXPathText");
        check(mn.contains("getInstance"), "method getInstance");
        check(mn.contains("getIncludeButSearch"), "method getIncludeButSearch");
        check(mn.contains("getExcludeButSearch"), "method getExcludeButSearch");
        check(mn.contains("getExclude"), "method getExclude");
        check(mn.contains("getIncludeSlashPolicy"), "method getIncludeSlashPolicy");
        check(mn.contains("getHereContextNodeIncludeButSearch"), "method getHereContextNodeIncludeButSearch");
        check(mn.contains("getHereContextNodeExcludeButSearch"), "method getHereContextNodeExcludeButSearch");
        check(mn.contains("getHereContextNodeExclude"), "method getHereContextNodeExclude");
        check(mn.contains("getBaseLocalName"), "method getBaseLocalName");
        check(mn.contains("getBaseNamespace"), "method getBaseNamespace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
