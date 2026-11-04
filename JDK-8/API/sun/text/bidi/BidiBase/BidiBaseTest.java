/*
 * @test
 * @summary sun.text.bidi.BidiBase conformance: class and members.
 * @run main BidiBaseTest
 */

public class BidiBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.bidi.BidiBase", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.bidi.BidiBase"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("Bidi_Abs"), "declares method Bidi_Abs");
        check(mnames.contains("Bidi_Min"), "declares method Bidi_Min");
        check(mnames.contains("DirPropFlag"), "declares method DirPropFlag");
        check(mnames.contains("DirPropFlagE"), "declares method DirPropFlagE");
        check(mnames.contains("DirPropFlagLR"), "declares method DirPropFlagLR");
        check(mnames.contains("DirPropFlagNC"), "declares method DirPropFlagNC");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
