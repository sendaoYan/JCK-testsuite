/*
 * @test
 * @summary sun.misc.ObjectInputFilter.FilterInfo conformance: interface and members.
 * @run main FilterInfoTest
 */

public class FilterInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.ObjectInputFilter$FilterInfo", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.ObjectInputFilter$FilterInfo"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("arrayLength"), "declares method arrayLength");
        check(mnames.contains("depth"), "declares method depth");
        check(mnames.contains("references"), "declares method references");
        check(mnames.contains("serialClass"), "declares method serialClass");
        check(mnames.contains("streamBytes"), "declares method streamBytes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
