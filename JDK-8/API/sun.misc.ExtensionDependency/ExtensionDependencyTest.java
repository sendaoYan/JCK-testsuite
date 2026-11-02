/*
 * @test
 * @summary sun.misc.ExtensionDependency conformance: class and members.
 * @run main ExtensionDependencyTest
 */

public class ExtensionDependencyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.ExtensionDependency", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.ExtensionDependency"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addExtensionInstallationProvider"), "declares method addExtensionInstallationProvider");
        check(mnames.contains("addNewExtensionsToClassLoader"), "declares method addNewExtensionsToClassLoader");
        check(mnames.contains("checkExtension"), "declares method checkExtension");
        check(mnames.contains("checkExtensionAgainst"), "declares method checkExtensionAgainst");
        check(mnames.contains("checkExtensionAgainstInstalled"), "declares method checkExtensionAgainstInstalled");
        check(mnames.contains("checkExtensionExists"), "declares method checkExtensionExists");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
