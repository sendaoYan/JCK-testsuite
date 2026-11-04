/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.implementations.Canonicalizer20010315 conformance: abstract class hierarchy and members.
 * @run main Canonicalizer20010315Test
 */
public class Canonicalizer20010315Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.c14n.implementations.Canonicalizer20010315", false, Canonicalizer20010315Test.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.c14n.implementations.CanonicalizerBase"), "superclass com.sun.org.apache.xml.internal.security.c14n.implementations.CanonicalizerBase");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("engineCanonicalizeXPathNodeSet"), "method engineCanonicalizeXPathNodeSet");
        check(mn.contains("engineCanonicalizeSubTree"), "method engineCanonicalizeSubTree");
        check(mn.contains("outputAttributesSubtree"), "method outputAttributesSubtree");
        check(mn.contains("outputAttributes"), "method outputAttributes");
        check(mn.contains("circumventBugIfNeeded"), "method circumventBugIfNeeded");
        check(mn.contains("handleParent"), "method handleParent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
