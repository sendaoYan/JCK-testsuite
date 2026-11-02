/*
 * @test
 * @summary sun.text.normalizer.NormalizerImpl conformance: class and members.
 * @run main NormalizerImplTest
 */

public class NormalizerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.NormalizerImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.NormalizerImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("addPropertyStarts"), "declares method addPropertyStarts");
        check(mnames.contains("canonicalDecomposeWithSingleQuotation"), "declares method canonicalDecomposeWithSingleQuotation");
        check(mnames.contains("combine"), "declares method combine");
        check(mnames.contains("compose"), "declares method compose");
        check(mnames.contains("composeHangul"), "declares method composeHangul");
        check(mnames.contains("composePart"), "declares method composePart");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
