/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.xinclude.XPointerSchema conformance: interface hierarchy and members.
 * @run main XPointerSchemaTest
 */
public class XPointerSchemaTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.xinclude.XPointerSchema", false, XPointerSchemaTest.class.getClassLoader());
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLComponent"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLComponent");
        check(hasInterface(c, "com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter"), "has interface com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setXPointerSchemaName"), "method setXPointerSchemaName");
        check(mn.contains("getXpointerSchemaName"), "method getXpointerSchemaName");
        check(mn.contains("setParent"), "method setParent");
        check(mn.contains("getParent"), "method getParent");
        check(mn.contains("setXPointerSchemaPointer"), "method setXPointerSchemaPointer");
        check(mn.contains("getXPointerSchemaPointer"), "method getXPointerSchemaPointer");
        check(mn.contains("isSubResourceIndentified"), "method isSubResourceIndentified");
        check(mn.contains("reset"), "method reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
