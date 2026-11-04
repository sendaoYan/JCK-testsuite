/*
 * @test
 * @summary com.sun.security.auth.PrincipalComparator conformance: hierarchy and declared members via reflection.
 * @run main PrincipalComparatorTest
 */

public class PrincipalComparatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.security.auth.PrincipalComparator", false, null);
        check(c.getName().equals("com.sun.security.auth.PrincipalComparator"), "binary name");
        check(c.isInterface() == true, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("implies"), "declares implies");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
