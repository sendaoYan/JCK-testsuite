/*
 * @test
 * @summary com.sun.xml.internal.bind.v2.runtime.unmarshaller.Scope conformance: class hierarchy and members.
 * @run main ScopeTest
 */

public class ScopeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.v2.runtime.unmarshaller.Scope", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.v2.runtime.unmarshaller.Scope"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("add"), "has method add");
        check(mn.contains("clean"), "has method clean");
        check(mn.contains("finish"), "has method finish");
        check(mn.contains("hasStarted"), "has method hasStarted");
        check(mn.contains("reset"), "has method reset");
        check(mn.contains("start"), "has method start");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
