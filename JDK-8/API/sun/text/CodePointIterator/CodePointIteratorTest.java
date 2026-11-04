/*
 * @test
 * @summary sun.text.CodePointIterator conformance: abstract class and members.
 * @run main CodePointIteratorTest
 */

public class CodePointIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.CodePointIterator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.CodePointIterator"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("charIndex"), "declares method charIndex");
        check(mnames.contains("create"), "declares method create");
        check(mnames.contains("next"), "declares method next");
        check(mnames.contains("prev"), "declares method prev");
        check(mnames.contains("setToLimit"), "declares method setToLimit");
        check(mnames.contains("setToStart"), "declares method setToStart");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
