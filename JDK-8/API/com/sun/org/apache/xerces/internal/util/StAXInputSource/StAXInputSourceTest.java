/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.util.StAXInputSource conformance: class hierarchy and members.
 * @run main StAXInputSourceTest
 */
public class StAXInputSourceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.util.StAXInputSource", false, StAXInputSourceTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource"), "superclass com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getXMLStreamReader"), "method getXMLStreamReader");
        check(mn.contains("getXMLEventReader"), "method getXMLEventReader");
        check(mn.contains("shouldConsumeRemainingContent"), "method shouldConsumeRemainingContent");
        check(mn.contains("setSystemId"), "method setSystemId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
