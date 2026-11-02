/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.output.XMLStreamWriterOutput conformance: class hierarchy and members.
 * @run main XMLStreamWriterOutputTest
 */

public class XMLStreamWriterOutputTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.output.XMLStreamWriterOutput", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.output.XMLStreamWriterOutput"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.runtime.output.XmlOutputAbstractImpl".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.runtime.output.XmlOutputAbstractImpl");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("attribute"), "has method attribute");
        check(mn.contains("beginStartTag"), "has method beginStartTag");
        check(mn.contains("create"), "has method create");
        check(mn.contains("endDocument"), "has method endDocument");
        check(mn.contains("endStartTag"), "has method endStartTag");
        check(mn.contains("endTag"), "has method endTag");
        check(mn.contains("initFIStAXWriterClass"), "has method initFIStAXWriterClass");
        check(mn.contains("initFastInfosetOutputClass"), "has method initFastInfosetOutputClass");
        check(mn.contains("initStAXExOutputClass"), "has method initStAXExOutputClass");
        check(mn.contains("initStAXExWriterClass"), "has method initStAXExWriterClass");
        check(mn.contains("startDocument"), "has method startDocument");
        check(mn.contains("text"), "has method text");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
