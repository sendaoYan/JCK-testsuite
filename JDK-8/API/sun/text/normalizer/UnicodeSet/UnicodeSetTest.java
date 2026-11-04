/*
 * @test
 * @summary sun.text.normalizer.UnicodeSet conformance: class and members.
 * @run main UnicodeSetTest
 */

public class UnicodeSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.UnicodeSet", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.UnicodeSet"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.text.normalizer.UnicodeMatcher"), "interface sun.text.normalizer.UnicodeMatcher");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("_appendToPat"), "declares method _appendToPat");
        check(mnames.contains("_generatePattern"), "declares method _generatePattern");
        check(mnames.contains("_toPattern"), "declares method _toPattern");
        check(mnames.contains("add"), "declares method add");
        check(mnames.contains("addAll"), "declares method addAll");
        check(mnames.contains("add_unchecked"), "declares method add_unchecked");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
