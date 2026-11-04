/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.implementations.CanonicalizerBase conformance: abstract class hierarchy and members.
 * @run main CanonicalizerBaseTest
 */
public class CanonicalizerBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.c14n.implementations.CanonicalizerBase", false, CanonicalizerBaseTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.security.c14n.CanonicalizerSpi"), "superclass com.sun.org.apache.xml.internal.security.c14n.CanonicalizerSpi");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("engineCanonicalizeSubTree"), "method engineCanonicalizeSubTree");
        check(mn.contains("engineCanonicalizeXPathNodeSet"), "method engineCanonicalizeXPathNodeSet");
        check(mn.contains("engineCanonicalize"), "method engineCanonicalize");
        check(mn.contains("setWriter"), "method setWriter");
        check(mn.contains("getWriter"), "method getWriter");
        check(mn.contains("canonicalizeSubTree"), "method canonicalizeSubTree");
        check(mn.contains("canonicalizeXPathNodeSet"), "method canonicalizeXPathNodeSet");
        check(mn.contains("isVisibleDO"), "method isVisibleDO");
        check(mn.contains("isVisibleInt"), "method isVisibleInt");
        check(mn.contains("isVisible"), "method isVisible");
        check(mn.contains("handleParent"), "method handleParent");
        check(mn.contains("getParentNameSpaces"), "method getParentNameSpaces");
        check(mn.contains("outputAttributes"), "method outputAttributes");
        check(mn.contains("outputAttributesSubtree"), "method outputAttributesSubtree");
        check(mn.contains("circumventBugIfNeeded"), "method circumventBugIfNeeded");
        check(mn.contains("outputAttrToWriter"), "method outputAttrToWriter");
        check(mn.contains("outputPItoWriter"), "method outputPItoWriter");
        check(mn.contains("outputCommentToWriter"), "method outputCommentToWriter");
        check(mn.contains("outputTextToWriter"), "method outputTextToWriter");
        check(mn.contains("getNullNode"), "method getNullNode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
