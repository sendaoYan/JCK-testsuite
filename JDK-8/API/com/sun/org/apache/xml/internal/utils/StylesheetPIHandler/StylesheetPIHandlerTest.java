/*
 * @test
 * @summary com.sun.org.apache.xml.internal.utils.StylesheetPIHandler conformance: class hierarchy and members.
 * @run main StylesheetPIHandlerTest
 */
public class StylesheetPIHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.utils.StylesheetPIHandler", false, StylesheetPIHandlerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("org.xml.sax.helpers.DefaultHandler"), "superclass org.xml.sax.helpers.DefaultHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setURIResolver"), "method setURIResolver");
        check(mn.contains("getURIResolver"), "method getURIResolver");
        check(mn.contains("getAssociatedStylesheet"), "method getAssociatedStylesheet");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("setBaseId"), "method setBaseId");
        check(mn.contains("getBaseId"), "method getBaseId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
