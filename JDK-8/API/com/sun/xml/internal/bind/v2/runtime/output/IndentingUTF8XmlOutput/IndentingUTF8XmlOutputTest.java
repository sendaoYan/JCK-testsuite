/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.output.IndentingUTF8XmlOutput conformance: class hierarchy and members.
 * @run main IndentingUTF8XmlOutputTest
 */

public class IndentingUTF8XmlOutputTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.output.IndentingUTF8XmlOutput", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.output.IndentingUTF8XmlOutput"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.bind.v2.runtime.output.UTF8XmlOutput".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.bind.v2.runtime.output.UTF8XmlOutput");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("beginStartTag"), "has method beginStartTag");
        check(mn.contains("endDocument"), "has method endDocument");
        check(mn.contains("endTag"), "has method endTag");
        check(mn.contains("indentEndTag"), "has method indentEndTag");
        check(mn.contains("indentStartTag"), "has method indentStartTag");
        check(mn.contains("printIndent"), "has method printIndent");
        check(mn.contains("text"), "has method text");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
