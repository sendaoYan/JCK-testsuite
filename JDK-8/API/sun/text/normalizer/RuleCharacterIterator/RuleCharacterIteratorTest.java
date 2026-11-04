/*
 * @test
 * @summary sun.text.normalizer.RuleCharacterIterator conformance: class and members.
 * @run main RuleCharacterIteratorTest
 */

public class RuleCharacterIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.RuleCharacterIterator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.RuleCharacterIterator"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("_advance"), "declares method _advance");
        check(mnames.contains("_current"), "declares method _current");
        check(mnames.contains("atEnd"), "declares method atEnd");
        check(mnames.contains("getPos"), "declares method getPos");
        check(mnames.contains("inVariable"), "declares method inVariable");
        check(mnames.contains("isEscaped"), "declares method isEscaped");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
