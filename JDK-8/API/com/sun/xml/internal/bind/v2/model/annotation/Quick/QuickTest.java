/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.model.annotation.Quick conformance: abstract class hierarchy and members.
 * @run main QuickTest
 */

public class QuickTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.model.annotation.Quick", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.model.annotation.Quick"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 3, "direct interface count");
        check(ifs.contains("com.sun.xml.internal.bind.v2.model.annotation.Locatable"), "has interface com.sun.xml.internal.bind.v2.model.annotation.Locatable");
        check(ifs.contains("com.sun.xml.internal.bind.v2.runtime.Location"), "has interface com.sun.xml.internal.bind.v2.runtime.Location");
        check(ifs.contains("java.lang.annotation.Annotation"), "has interface java.lang.annotation.Annotation");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAnnotation"), "has method getAnnotation");
        check(mn.contains("getLocation"), "has method getLocation");
        check(mn.contains("getUpstream"), "has method getUpstream");
        check(mn.contains("newInstance"), "has method newInstance");
        check(mn.contains("toString"), "has method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
