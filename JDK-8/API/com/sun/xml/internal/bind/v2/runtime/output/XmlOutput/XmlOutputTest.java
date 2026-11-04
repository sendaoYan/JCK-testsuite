/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.output.XmlOutput conformance: interface hierarchy and members.
 * @run main XmlOutputTest
 */

public class XmlOutputTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.output.XmlOutput", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.output.XmlOutput"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("attribute"), "has method attribute");
        check(mn.contains("beginStartTag"), "has method beginStartTag");
        check(mn.contains("endDocument"), "has method endDocument");
        check(mn.contains("endStartTag"), "has method endStartTag");
        check(mn.contains("endTag"), "has method endTag");
        check(mn.contains("startDocument"), "has method startDocument");
        check(mn.contains("text"), "has method text");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
