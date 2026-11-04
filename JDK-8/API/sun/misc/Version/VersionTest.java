/*
 * @test
 * @summary sun.misc.Version conformance: class and members.
 * @run main VersionTest
 */

public class VersionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.Version", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.Version"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getJdkSpecialVersion"), "declares method getJdkSpecialVersion");
        check(mnames.contains("getJdkVersionInfo"), "declares method getJdkVersionInfo");
        check(mnames.contains("getJvmSpecialVersion"), "declares method getJvmSpecialVersion");
        check(mnames.contains("getJvmVersionInfo"), "declares method getJvmVersionInfo");
        check(mnames.contains("init"), "declares method init");
        check(mnames.contains("initVersions"), "declares method initVersions");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
