/*
 * @test
 * @summary sun.util.locale.ParseStatus conformance: class and members.
 * @run main ParseStatusTest
 */

public class ParseStatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.ParseStatus", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.locale.ParseStatus"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getErrorIndex"), "declares method getErrorIndex");
        check(mnames.contains("getErrorMessage"), "declares method getErrorMessage");
        check(mnames.contains("getParseLength"), "declares method getParseLength");
        check(mnames.contains("isError"), "declares method isError");
        check(mnames.contains("reset"), "declares method reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
