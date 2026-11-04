/*
 * @test
 * @summary sun.text.normalizer.NormalizerBase.Mode conformance: class and members.
 * @run main ModeTest
 */

public class ModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.NormalizerBase$Mode", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.NormalizerBase$Mode"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getMask"), "declares method getMask");
        check(mnames.contains("getMinC"), "declares method getMinC");
        check(mnames.contains("getNextBoundary"), "declares method getNextBoundary");
        check(mnames.contains("getPrevBoundary"), "declares method getPrevBoundary");
        check(mnames.contains("isNFSkippable"), "declares method isNFSkippable");
        check(mnames.contains("normalize"), "declares method normalize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
