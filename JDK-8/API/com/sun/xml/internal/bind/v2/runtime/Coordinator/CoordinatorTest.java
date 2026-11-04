/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.Coordinator conformance: abstract class hierarchy and members.
 * @run main CoordinatorTest
 */

public class CoordinatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.Coordinator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.Coordinator"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "direct interface count");
        check(ifs.contains("javax.xml.bind.ValidationEventHandler"), "has interface javax.xml.bind.ValidationEventHandler");
        check(ifs.contains("org.xml.sax.ErrorHandler"), "has interface org.xml.sax.ErrorHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("_getInstance"), "has method _getInstance");
        check(mn.contains("containsAdapter"), "has method containsAdapter");
        check(mn.contains("error"), "has method error");
        check(mn.contains("fatalError"), "has method fatalError");
        check(mn.contains("getAdapter"), "has method getAdapter");
        check(mn.contains("getLocation"), "has method getLocation");
        check(mn.contains("popCoordinator"), "has method popCoordinator");
        check(mn.contains("propagateEvent"), "has method propagateEvent");
        check(mn.contains("pushCoordinator"), "has method pushCoordinator");
        check(mn.contains("putAdapter"), "has method putAdapter");
        check(mn.contains("warning"), "has method warning");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
