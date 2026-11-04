/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XSWildcardDecl conformance: class hierarchy and members.
 * @run main XSWildcardDeclTest
 */
public class XSWildcardDeclTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XSWildcardDecl", false, XSWildcardDeclTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSWildcard"), "has interface com.sun.org.apache.xerces.internal.xs.XSWildcard");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("allowNamespace"), "method allowNamespace");
        check(mn.contains("isSubsetOf"), "method isSubsetOf");
        check(mn.contains("weakerProcessContents"), "method weakerProcessContents");
        check(mn.contains("performUnionWith"), "method performUnionWith");
        check(mn.contains("performIntersectionWith"), "method performIntersectionWith");
        check(mn.contains("intersect2sets"), "method intersect2sets");
        check(mn.contains("union2sets"), "method union2sets");
        check(mn.contains("subset2sets"), "method subset2sets");
        check(mn.contains("elementInSet"), "method elementInSet");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNamespace"), "method getNamespace");
        check(mn.contains("getConstraintType"), "method getConstraintType");
        check(mn.contains("getNsConstraintList"), "method getNsConstraintList");
        check(mn.contains("getProcessContents"), "method getProcessContents");
        check(mn.contains("getProcessContentsAsString"), "method getProcessContentsAsString");
        check(mn.contains("getAnnotation"), "method getAnnotation");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        check(mn.contains("getNamespaceItem"), "method getNamespaceItem");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
