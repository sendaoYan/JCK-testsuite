/*
 * @test
 * @summary com.sun.xml.internal.bind.api.ErrorListener conformance: interface hierarchy and members.
 * @run main ErrorListenerTest
 */

public class ErrorListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.bind.api.ErrorListener", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.bind.api.ErrorListener"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("org.xml.sax.ErrorHandler"), "has interface org.xml.sax.ErrorHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("error"), "has method error");
        check(mn.contains("fatalError"), "has method fatalError");
        check(mn.contains("info"), "has method info");
        check(mn.contains("warning"), "has method warning");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
