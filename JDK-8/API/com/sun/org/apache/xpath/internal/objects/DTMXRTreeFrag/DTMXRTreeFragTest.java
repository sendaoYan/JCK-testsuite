/*
 * @test
 * @summary com.sun.org.apache.xpath.internal.objects.DTMXRTreeFrag conformance: reflection.
 * @run main DTMXRTreeFragTest
 */

public class DTMXRTreeFragTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xpath.internal.objects.DTMXRTreeFrag", false, DTMXRTreeFragTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("DTMXRTreeFrag"), "simple name DTMXRTreeFrag");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "destruct"), "method destruct");
        check(has(c, "getDTMIdentity"), "method getDTMIdentity");
        check(has(c, "hashCode"), "method hashCode");
        check(has(c, "equals"), "method equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
