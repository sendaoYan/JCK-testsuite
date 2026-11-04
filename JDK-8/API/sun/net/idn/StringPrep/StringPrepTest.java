/*
 * @test
 * @summary sun.net.idn.StringPrep conformance: class and members.
 * @run main StringPrepTest
 */

public class StringPrepTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.idn.StringPrep", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.idn.StringPrep"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getCodePointValue"), "declares method getCodePointValue");
        check(mnames.contains("getValues"), "declares method getValues");
        check(mnames.contains("getVersionInfo"), "declares method getVersionInfo");
        check(mnames.contains("map"), "declares method map");
        check(mnames.contains("normalize"), "declares method normalize");
        check(mnames.contains("prepare"), "declares method prepare");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
