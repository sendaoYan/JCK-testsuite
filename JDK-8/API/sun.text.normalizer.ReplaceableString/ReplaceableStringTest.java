/*
 * @test
 * @summary sun.text.normalizer.ReplaceableString conformance: class and members.
 * @run main ReplaceableStringTest
 */

public class ReplaceableStringTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.ReplaceableString", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.ReplaceableString"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.text.normalizer.Replaceable"), "interface sun.text.normalizer.Replaceable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("charAt"), "declares method charAt");
        check(mnames.contains("getChars"), "declares method getChars");
        check(mnames.contains("length"), "declares method length");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
