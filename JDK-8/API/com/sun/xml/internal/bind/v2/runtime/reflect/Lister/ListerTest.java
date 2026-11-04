/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.reflect.Lister conformance: abstract class hierarchy and members.
 * @run main ListerTest
 */

public class ListerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.reflect.Lister", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.reflect.Lister"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addToPack"), "has method addToPack");
        check(mn.contains("create"), "has method create");
        check(mn.contains("endPacking"), "has method endPacking");
        check(mn.contains("getArrayLister"), "has method getArrayLister");
        check(mn.contains("getErrorInstance"), "has method getErrorInstance");
        check(mn.contains("getImplClass"), "has method getImplClass");
        check(mn.contains("iterator"), "has method iterator");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("startPacking"), "has method startPacking");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
