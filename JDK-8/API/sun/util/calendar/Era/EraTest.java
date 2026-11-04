/*
 * @test
 * @summary sun.util.calendar.Era conformance: class and members.
 * @run main EraTest
 */

public class EraTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.calendar.Era", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.calendar.Era"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("equals"), "declares method equals");
        check(mnames.contains("getAbbreviation"), "declares method getAbbreviation");
        check(mnames.contains("getDiaplayAbbreviation"), "declares method getDiaplayAbbreviation");
        check(mnames.contains("getDisplayName"), "declares method getDisplayName");
        check(mnames.contains("getName"), "declares method getName");
        check(mnames.contains("getSince"), "declares method getSince");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
