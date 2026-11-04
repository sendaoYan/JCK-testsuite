/*
 * @test
 * @summary sun.misc.Service conformance: class and members.
 * @run main ServiceTest
 */

public class ServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.Service", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.Service"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("fail"), "declares method fail");
        check(mnames.contains("installedProviders"), "declares method installedProviders");
        check(mnames.contains("parse"), "declares method parse");
        check(mnames.contains("parseLine"), "declares method parseLine");
        check(mnames.contains("providers"), "declares method providers");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
