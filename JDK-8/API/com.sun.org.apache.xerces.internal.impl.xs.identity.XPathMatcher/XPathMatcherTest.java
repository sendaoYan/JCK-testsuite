/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.identity.XPathMatcher conformance: class hierarchy and members.
 * @run main XPathMatcherTest
 */
public class XPathMatcherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.identity.XPathMatcher", false, XPathMatcherTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("isMatched"), "method isMatched");
        check(mn.contains("handleContent"), "method handleContent");
        check(mn.contains("matched"), "method matched");
        check(mn.contains("startDocumentFragment"), "method startDocumentFragment");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
