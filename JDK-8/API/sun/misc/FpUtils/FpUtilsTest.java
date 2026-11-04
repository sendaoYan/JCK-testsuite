/*
 * @test
 * @summary sun.misc.FpUtils conformance: class and members.
 * @run main FpUtilsTest
 */

public class FpUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.FpUtils", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.FpUtils"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("copySign"), "declares method copySign");
        check(mnames.contains("getExponent"), "declares method getExponent");
        check(mnames.contains("ilogb"), "declares method ilogb");
        check(mnames.contains("isFinite"), "declares method isFinite");
        check(mnames.contains("isInfinite"), "declares method isInfinite");
        check(mnames.contains("isNaN"), "declares method isNaN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
