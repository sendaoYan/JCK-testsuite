/*
 * @test
 * @summary sun.management.VMOptionCompositeData conformance: class and members.
 * @run main VMOptionCompositeDataTest
 */

public class VMOptionCompositeDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.VMOptionCompositeData", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.VMOptionCompositeData"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.management.LazyCompositeData"), "superclass sun.management.LazyCompositeData");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getCompositeData"), "declares method getCompositeData");
        check(mnames.contains("getName"), "declares method getName");
        check(mnames.contains("getOrigin"), "declares method getOrigin");
        check(mnames.contains("getVMOption"), "declares method getVMOption");
        check(mnames.contains("getVMOptionCompositeType"), "declares method getVMOptionCompositeType");
        check(mnames.contains("getValue"), "declares method getValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
