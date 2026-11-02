/*
 * @test
 * @summary sun.text.normalizer.TrieIterator conformance: class and members.
 * @run main TrieIteratorTest
 */

public class TrieIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.text.normalizer.TrieIterator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.text.normalizer.TrieIterator"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("sun.text.normalizer.RangeValueIterator"), "interface sun.text.normalizer.RangeValueIterator");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("calculateNextBMPElement"), "declares method calculateNextBMPElement");
        check(mnames.contains("calculateNextSupplementaryElement"), "declares method calculateNextSupplementaryElement");
        check(mnames.contains("checkBlock"), "declares method checkBlock");
        check(mnames.contains("checkBlockDetail"), "declares method checkBlockDetail");
        check(mnames.contains("checkNullNextTrailIndex"), "declares method checkNullNextTrailIndex");
        check(mnames.contains("checkTrailBlock"), "declares method checkTrailBlock");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
