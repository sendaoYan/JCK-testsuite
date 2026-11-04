/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.tools.FI_DOM_Or_XML_DOM_SAX_SAXEvent conformance: class hierarchy and members.
 * @run main FI_DOM_Or_XML_DOM_SAX_SAXEventTest
 */

public class FI_DOM_Or_XML_DOM_SAX_SAXEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.tools.FI_DOM_Or_XML_DOM_SAX_SAXEvent", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.tools.FI_DOM_Or_XML_DOM_SAX_SAXEvent"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.fastinfoset.tools.TransformInputOutput".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.fastinfoset.tools.TransformInputOutput");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("main"), "has method main");
        check(mn.contains("parse"), "has method parse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
