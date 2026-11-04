/*
 * @test
 * @summary sun.misc.ASCIICaseInsensitiveComparator conformance: class and members.
 * @run main ASCIICaseInsensitiveComparatorTest
 */

public class ASCIICaseInsensitiveComparatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.ASCIICaseInsensitiveComparator", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.ASCIICaseInsensitiveComparator"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.util.Comparator"), "interface java.util.Comparator");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("compare"), "declares method compare");
        check(mnames.contains("isLower"), "declares method isLower");
        check(mnames.contains("isUpper"), "declares method isUpper");
        check(mnames.contains("lowerCaseHashCode"), "declares method lowerCaseHashCode");
        check(mnames.contains("toLower"), "declares method toLower");
        check(mnames.contains("toUpper"), "declares method toUpper");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
