/*
 * @test
 * @summary sun.util.CoreResourceBundleControl conformance: class and members.
 * @run main CoreResourceBundleControlTest
 */

public class CoreResourceBundleControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.CoreResourceBundleControl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.CoreResourceBundleControl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.ResourceBundle$Control"), "superclass java.util.ResourceBundle$Control");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getCandidateLocales"), "declares method getCandidateLocales");
        check(mnames.contains("getRBControlInstance"), "declares method getRBControlInstance");
        check(mnames.contains("getTimeToLive"), "declares method getTimeToLive");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
