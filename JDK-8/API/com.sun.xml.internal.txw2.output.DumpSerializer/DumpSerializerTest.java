/*
 * @test
 * @summary com.sun.xml.internal.txw2.output.DumpSerializer conformance: class hierarchy and members.
 * @run main DumpSerializerTest
 */

public class DumpSerializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.txw2.output.DumpSerializer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.txw2.output.DumpSerializer"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.txw2.output.XmlSerializer"), "has interface com.sun.xml.internal.txw2.output.XmlSerializer");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("beginStartTag"), "has method beginStartTag");
        check(mn.contains("cdata"), "has method cdata");
        check(mn.contains("comment"), "has method comment");
        check(mn.contains("endDocument"), "has method endDocument");
        check(mn.contains("endStartTag"), "has method endStartTag");
        check(mn.contains("endTag"), "has method endTag");
        check(mn.contains("flush"), "has method flush");
        check(mn.contains("startDocument"), "has method startDocument");
        check(mn.contains("text"), "has method text");
        check(mn.contains("writeAttribute"), "has method writeAttribute");
        check(mn.contains("writeXmlns"), "has method writeXmlns");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
