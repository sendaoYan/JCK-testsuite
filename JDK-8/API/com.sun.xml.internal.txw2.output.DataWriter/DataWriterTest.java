/*
 * @test
 * @summary com.sun.xml.internal.txw2.output.DataWriter conformance: class hierarchy and members.
 * @run main DataWriterTest
 */

public class DataWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.txw2.output.DataWriter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.txw2.output.DataWriter"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.txw2.output.XMLWriter".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.txw2.output.XMLWriter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("characters"), "has method characters");
        check(mn.contains("comment"), "has method comment");
        check(mn.contains("doIndent"), "has method doIndent");
        check(mn.contains("endElement"), "has method endElement");
        check(mn.contains("getIndentStep"), "has method getIndentStep");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("setIndentStep"), "has method setIndentStep");
        check(mn.contains("startElement"), "has method startElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
