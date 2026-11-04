/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.stax.events.StAXEventWriter conformance: class hierarchy and members.
 * @run main StAXEventWriterTest
 */

public class StAXEventWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.stax.events.StAXEventWriter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.stax.events.StAXEventWriter"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.XMLEventWriter"), "has interface javax.xml.stream.XMLEventWriter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("add"), "has method add");
        check(mn.contains("close"), "has method close");
        check(mn.contains("flush"), "has method flush");
        check(mn.contains("getNamespaceContext"), "has method getNamespaceContext");
        check(mn.contains("getPrefix"), "has method getPrefix");
        check(mn.contains("setDefaultNamespace"), "has method setDefaultNamespace");
        check(mn.contains("setNamespaceContext"), "has method setNamespaceContext");
        check(mn.contains("setPrefix"), "has method setPrefix");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
