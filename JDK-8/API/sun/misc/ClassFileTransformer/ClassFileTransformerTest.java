/*
 * @test
 * @summary sun.misc.ClassFileTransformer conformance: abstract class and members.
 * @run main ClassFileTransformerTest
 */

public class ClassFileTransformerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.ClassFileTransformer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.ClassFileTransformer"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("add"), "declares method add");
        check(mnames.contains("getTransformers"), "declares method getTransformers");
        check(mnames.contains("transform"), "declares method transform");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
