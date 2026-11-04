/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.implementations.CanonicalizerPhysical conformance: class hierarchy and members.
 * @run main CanonicalizerPhysicalTest
 */
public class CanonicalizerPhysicalTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.c14n.implementations.CanonicalizerPhysical", false, CanonicalizerPhysicalTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.c14n.implementations.CanonicalizerBase"), "superclass com.sun.org.apache.xml.internal.security.c14n.implementations.CanonicalizerBase");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("engineCanonicalizeXPathNodeSet"), "method engineCanonicalizeXPathNodeSet");
        check(mn.contains("engineCanonicalizeSubTree"), "method engineCanonicalizeSubTree");
        check(mn.contains("outputAttributesSubtree"), "method outputAttributesSubtree");
        check(mn.contains("outputAttributes"), "method outputAttributes");
        check(mn.contains("circumventBugIfNeeded"), "method circumventBugIfNeeded");
        check(mn.contains("handleParent"), "method handleParent");
        check(mn.contains("engineGetURI"), "method engineGetURI");
        check(mn.contains("engineGetIncludeComments"), "method engineGetIncludeComments");
        check(mn.contains("outputPItoWriter"), "method outputPItoWriter");
        check(mn.contains("outputCommentToWriter"), "method outputCommentToWriter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
