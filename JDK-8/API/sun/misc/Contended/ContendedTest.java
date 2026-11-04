/*
 * @test
 * @summary sun.misc.Contended conformance: annotation type and elements.
 * @run main ContendedTest
 */

public class ContendedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.Contended", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.Contended"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.isAnnotation(), "is annotation");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.annotation.Annotation"), "interface java.lang.annotation.Annotation");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("value"), "declares method value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
