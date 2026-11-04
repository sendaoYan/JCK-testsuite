/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.AssociationMap conformance: class hierarchy and members.
 * @run main AssociationMapTest
 */

public class AssociationMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.AssociationMap", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.AssociationMap"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addInner"), "has method addInner");
        check(mn.contains("addOuter"), "has method addOuter");
        check(mn.contains("addUsed"), "has method addUsed");
        check(mn.contains("byElement"), "has method byElement");
        check(mn.contains("byPeer"), "has method byPeer");
        check(mn.contains("getInnerPeer"), "has method getInnerPeer");
        check(mn.contains("getOuterPeer"), "has method getOuterPeer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
