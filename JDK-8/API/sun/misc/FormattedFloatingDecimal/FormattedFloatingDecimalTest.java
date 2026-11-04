/*
 * @test
 * @summary sun.misc.FormattedFloatingDecimal conformance: class and members.
 * @run main FormattedFloatingDecimalTest
 */

public class FormattedFloatingDecimalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.FormattedFloatingDecimal", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.FormattedFloatingDecimal"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("applyPrecision"), "declares method applyPrecision");
        check(mnames.contains("create"), "declares method create");
        check(mnames.contains("fillCompatible"), "declares method fillCompatible");
        check(mnames.contains("fillDecimal"), "declares method fillDecimal");
        check(mnames.contains("fillScientific"), "declares method fillScientific");
        check(mnames.contains("getBuffer"), "declares method getBuffer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
