/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xs.XSNamespaceItem conformance: interface hierarchy and members.
 * @run main XSNamespaceItemTest
 */
public class XSNamespaceItemTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xs.XSNamespaceItem", false, XSNamespaceItemTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getSchemaNamespace"), "method getSchemaNamespace");
        check(mn.contains("getComponents"), "method getComponents");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        check(mn.contains("getElementDeclaration"), "method getElementDeclaration");
        check(mn.contains("getAttributeDeclaration"), "method getAttributeDeclaration");
        check(mn.contains("getTypeDefinition"), "method getTypeDefinition");
        check(mn.contains("getAttributeGroup"), "method getAttributeGroup");
        check(mn.contains("getModelGroupDefinition"), "method getModelGroupDefinition");
        check(mn.contains("getNotationDeclaration"), "method getNotationDeclaration");
        check(mn.contains("getDocumentLocations"), "method getDocumentLocations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
