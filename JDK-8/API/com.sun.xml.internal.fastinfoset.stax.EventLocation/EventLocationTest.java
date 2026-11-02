/*
 * @test
 * @summary com.sun.xml.internal.fastinfoset.stax.EventLocation conformance: class hierarchy and members.
 * @run main EventLocationTest
 */

public class EventLocationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.fastinfoset.stax.EventLocation", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.fastinfoset.stax.EventLocation"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.Location"), "has interface javax.xml.stream.Location");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getCharacterOffset"), "has method getCharacterOffset");
        check(mn.contains("getColumnNumber"), "has method getColumnNumber");
        check(mn.contains("getLineNumber"), "has method getLineNumber");
        check(mn.contains("getNilLocation"), "has method getNilLocation");
        check(mn.contains("getPublicId"), "has method getPublicId");
        check(mn.contains("getSystemId"), "has method getSystemId");
        check(mn.contains("setCharacterOffset"), "has method setCharacterOffset");
        check(mn.contains("setColumnNumber"), "has method setColumnNumber");
        check(mn.contains("setLineNumber"), "has method setLineNumber");
        check(mn.contains("setPublicId"), "has method setPublicId");
        check(mn.contains("setSystemId"), "has method setSystemId");
        check(mn.contains("toString"), "has method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
