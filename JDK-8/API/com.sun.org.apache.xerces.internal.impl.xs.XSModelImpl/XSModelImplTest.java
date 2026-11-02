/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.XSModelImpl conformance: class hierarchy and members.
 * @run main XSModelImplTest
 */
public class XSModelImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.XSModelImpl", false, XSModelImplTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("java.util.AbstractList"), "superclass java.util.AbstractList");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSModel"), "has interface com.sun.org.apache.xerces.internal.xs.XSModel");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xs.XSNamespaceItemList"), "has interface com.sun.org.apache.xerces.internal.xs.XSNamespaceItemList");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getNamespaces"), "method getNamespaces");
        check(mn.contains("getNamespaceItems"), "method getNamespaceItems");
        check(mn.contains("getComponents"), "method getComponents");
        check(mn.contains("getComponentsByNamespace"), "method getComponentsByNamespace");
        check(mn.contains("getTypeDefinition"), "method getTypeDefinition");
        check(mn.contains("getAttributeDeclaration"), "method getAttributeDeclaration");
        check(mn.contains("getElementDeclaration"), "method getElementDeclaration");
        check(mn.contains("getAttributeGroup"), "method getAttributeGroup");
        check(mn.contains("getModelGroupDefinition"), "method getModelGroupDefinition");
        check(mn.contains("getNotationDeclaration"), "method getNotationDeclaration");
        check(mn.contains("getAnnotations"), "method getAnnotations");
        check(mn.contains("hasIDConstraints"), "method hasIDConstraints");
        check(mn.contains("getSubstitutionGroup"), "method getSubstitutionGroup");
        check(mn.contains("getLength"), "method getLength");
        check(mn.contains("item"), "method item");
        check(mn.contains("get"), "method get");
        check(mn.contains("size"), "method size");
        check(mn.contains("iterator"), "method iterator");
        check(mn.contains("listIterator"), "method listIterator");
        check(mn.contains("toArray"), "method toArray");
        check(mn.contains("access$000"), "method access$000");
        check(mn.contains("access$100"), "method access$100");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
