/*
 * @test
 * @summary com.sun.xml.internal.txw2.Document conformance: class hierarchy and members.
 * @run main DocumentTest
 */

public class DocumentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.txw2.Document", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.txw2.Document"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addDatatypeWriter"), "has method addDatatypeWriter");
        check(mn.contains("assignNewId"), "has method assignNewId");
        check(mn.contains("fixPrefix"), "has method fixPrefix");
        check(mn.contains("flush"), "has method flush");
        check(mn.contains("newPrefix"), "has method newPrefix");
        check(mn.contains("run"), "has method run");
        check(mn.contains("setFirstContent"), "has method setFirstContent");
        check(mn.contains("writeValue"), "has method writeValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
