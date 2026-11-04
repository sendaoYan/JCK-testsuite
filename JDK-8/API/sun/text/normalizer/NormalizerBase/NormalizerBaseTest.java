/*
 * @test
 * @summary sun.text.normalizer.NormalizerBase conformance: class and members.
 * @run main NormalizerBaseTest
 */

public class NormalizerBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.NormalizerBase", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.NormalizerBase"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("clearBuffer"), "declares method clearBuffer");
        check(mnames.contains("clone"), "declares method clone");
        check(mnames.contains("compose"), "declares method compose");
        check(mnames.contains("current"), "declares method current");
        check(mnames.contains("decompose"), "declares method decompose");
        check(mnames.contains("endIndex"), "declares method endIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
