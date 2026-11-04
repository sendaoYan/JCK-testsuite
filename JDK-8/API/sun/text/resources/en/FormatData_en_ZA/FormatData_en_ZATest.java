/*
 * @test
 * @summary sun.text.resources.en.FormatData_en_ZA conformance: class and members.
 * @run main FormatData_en_ZATest
 */

public class FormatData_en_ZATest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.resources.en.FormatData_en_ZA", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.resources.en.FormatData_en_ZA"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.resources.ParallelListResourceBundle"), "superclass sun.util.resources.ParallelListResourceBundle");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getContents"), "declares method getContents");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
