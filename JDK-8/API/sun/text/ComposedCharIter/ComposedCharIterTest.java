/*
 * @test
 * @summary sun.text.ComposedCharIter conformance: class and members.
 * @run main ComposedCharIterTest
 */

public class ComposedCharIterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.ComposedCharIter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.ComposedCharIter"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("decomposition"), "declares method decomposition");
        check(mnames.contains("next"), "declares method next");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
