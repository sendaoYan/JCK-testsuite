/*
 * @test
 * @summary sun.management.VMManagement conformance: interface and members.
 * @run main VMManagementTest
 */

public class VMManagementTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.VMManagement", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.VMManagement"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getAvailableProcessors"), "declares method getAvailableProcessors");
        check(mnames.contains("getBootClassPath"), "declares method getBootClassPath");
        check(mnames.contains("getClassInitializationTime"), "declares method getClassInitializationTime");
        check(mnames.contains("getClassLoadingTime"), "declares method getClassLoadingTime");
        check(mnames.contains("getClassPath"), "declares method getClassPath");
        check(mnames.contains("getClassVerificationTime"), "declares method getClassVerificationTime");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
