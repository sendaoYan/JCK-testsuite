/*
 * @test
 * @summary sun.text.normalizer.ReplaceableUCharacterIterator conformance: class and members.
 * @run main ReplaceableUCharacterIteratorTest
 */

public class ReplaceableUCharacterIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.ReplaceableUCharacterIterator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.ReplaceableUCharacterIterator"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.text.normalizer.UCharacterIterator"), "superclass sun.text.normalizer.UCharacterIterator");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("clone"), "declares method clone");
        check(mnames.contains("current"), "declares method current");
        check(mnames.contains("getIndex"), "declares method getIndex");
        check(mnames.contains("getLength"), "declares method getLength");
        check(mnames.contains("getText"), "declares method getText");
        check(mnames.contains("next"), "declares method next");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
