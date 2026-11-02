/*
 * @test
 * @summary sun.text.normalizer.UCharacterIterator conformance: abstract class and members.
 * @run main UCharacterIteratorTest
 */

public class UCharacterIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.UCharacterIterator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.UCharacterIterator"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("clone"), "declares method clone");
        check(mnames.contains("current"), "declares method current");
        check(mnames.contains("getIndex"), "declares method getIndex");
        check(mnames.contains("getInstance"), "declares method getInstance");
        check(mnames.contains("getLength"), "declares method getLength");
        check(mnames.contains("getText"), "declares method getText");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
