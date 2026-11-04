/*
 * @test
 * @summary sun.management.HotspotClassLoadingMBean conformance: interface and members.
 * @run main HotspotClassLoadingMBeanTest
 */

public class HotspotClassLoadingMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.HotspotClassLoadingMBean", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.HotspotClassLoadingMBean"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getClassInitializationTime"), "declares method getClassInitializationTime");
        check(mnames.contains("getClassLoadingTime"), "declares method getClassLoadingTime");
        check(mnames.contains("getClassVerificationTime"), "declares method getClassVerificationTime");
        check(mnames.contains("getInitializedClassCount"), "declares method getInitializedClassCount");
        check(mnames.contains("getInternalClassLoadingCounters"), "declares method getInternalClassLoadingCounters");
        check(mnames.contains("getLoadedClassSize"), "declares method getLoadedClassSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
