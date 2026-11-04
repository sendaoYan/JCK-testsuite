/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.unmarshaller.InterningXmlVisitor conformance: class hierarchy and members.
 * @run main InterningXmlVisitorTest
 */

public class InterningXmlVisitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.unmarshaller.InterningXmlVisitor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.unmarshaller.InterningXmlVisitor"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.runtime.unmarshaller.XmlVisitor"), "has interface com.sun.xml.internal.bind.v2.runtime.unmarshaller.XmlVisitor");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("endDocument"), "has method endDocument");
        check(mn.contains("endElement"), "has method endElement");
        check(mn.contains("endPrefixMapping"), "has method endPrefixMapping");
        check(mn.contains("getContext"), "has method getContext");
        check(mn.contains("getPredictor"), "has method getPredictor");
        check(mn.contains("intern"), "has method intern");
        check(mn.contains("startDocument"), "has method startDocument");
        check(mn.contains("startElement"), "has method startElement");
        check(mn.contains("startPrefixMapping"), "has method startPrefixMapping");
        check(mn.contains("text"), "has method text");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
