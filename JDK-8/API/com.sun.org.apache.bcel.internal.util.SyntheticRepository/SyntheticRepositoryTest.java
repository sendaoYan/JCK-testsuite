/*
 * @test
 * @summary com.sun.org.apache.bcel.internal.util.SyntheticRepository conformance: reflection.
 * @run main SyntheticRepositoryTest
 */

public class SyntheticRepositoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.bcel.internal.util.SyntheticRepository", false, SyntheticRepositoryTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("SyntheticRepository"), "simple name SyntheticRepository");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 1, "interfaces count 1");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.bcel.internal.util.Repository"), "interface com.sun.org.apache.bcel.internal.util.Repository");
        check(has(c, "getInstance"), "method getInstance");
        check(has(c, "storeClass"), "method storeClass");
        check(has(c, "removeClass"), "method removeClass");
        check(has(c, "findClass"), "method findClass");
        check(has(c, "loadClass"), "method loadClass");
        check(has(c, "clear"), "method clear");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
