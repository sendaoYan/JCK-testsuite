/*
 * @test
 * @summary sun.launcher.LauncherHelper conformance: enum constants and members.
 * @run main LauncherHelperTest
 */

public class LauncherHelperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.launcher.LauncherHelper", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.launcher.LauncherHelper"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("abort"), "declares method abort");
        check(mnames.contains("appendVmErgoMessage"), "declares method appendVmErgoMessage");
        check(mnames.contains("appendVmSelectMessage"), "declares method appendVmSelectMessage");
        check(mnames.contains("appendVmSynonymMessage"), "declares method appendVmSynonymMessage");
        check(mnames.contains("checkAndLoadMain"), "declares method checkAndLoadMain");
        check(mnames.contains("expandArgs"), "declares method expandArgs");
        java.util.Set<String> consts = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) consts.add(f.getName());
        check(consts.contains("INSTANCE"), "enum constant INSTANCE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
