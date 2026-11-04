/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.unmarshaller.XmlVisitor conformance: interface hierarchy and members.
 * @run main XmlVisitorTest
 */

public class XmlVisitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.unmarshaller.XmlVisitor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.unmarshaller.XmlVisitor"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("endDocument"), "has method endDocument");
        check(mn.contains("endElement"), "has method endElement");
        check(mn.contains("endPrefixMapping"), "has method endPrefixMapping");
        check(mn.contains("getContext"), "has method getContext");
        check(mn.contains("getPredictor"), "has method getPredictor");
        check(mn.contains("startDocument"), "has method startDocument");
        check(mn.contains("startElement"), "has method startElement");
        check(mn.contains("startPrefixMapping"), "has method startPrefixMapping");
        check(mn.contains("text"), "has method text");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
