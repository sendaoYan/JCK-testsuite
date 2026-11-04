/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.transforms.params.XPath2FilterContainer conformance: class hierarchy and members.
 * @run main XPath2FilterContainerTest
 */
public class XPath2FilterContainerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.transforms.params.XPath2FilterContainer", false, XPath2FilterContainerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.utils.ElementProxy"), "superclass com.sun.org.apache.xml.internal.security.utils.ElementProxy");
        check(hasInterface(c, "com.sun.org.apache.xml.internal.security.transforms.TransformParam"), "has interface com.sun.org.apache.xml.internal.security.transforms.TransformParam");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("newInstanceIntersect"), "method newInstanceIntersect");
        check(mn.contains("newInstanceSubtract"), "method newInstanceSubtract");
        check(mn.contains("newInstanceUnion"), "method newInstanceUnion");
        check(mn.contains("newInstances"), "method newInstances");
        check(mn.contains("newInstance"), "method newInstance");
        check(mn.contains("isIntersect"), "method isIntersect");
        check(mn.contains("isSubtract"), "method isSubtract");
        check(mn.contains("isUnion"), "method isUnion");
        check(mn.contains("getXPathFilterStr"), "method getXPathFilterStr");
        check(mn.contains("getXPathFilterTextNode"), "method getXPathFilterTextNode");
        check(mn.contains("getBaseLocalName"), "method getBaseLocalName");
        check(mn.contains("getBaseNamespace"), "method getBaseNamespace");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
