/*
 * @test
 * @summary com.sun.xml.internal.txw2.TypedXmlWriter conformance: interface hierarchy and members.
 * @run main TypedXmlWriterTest
 */

public class TypedXmlWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.txw2.TypedXmlWriter", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.txw2.TypedXmlWriter"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("_attribute"), "has method _attribute");
        check(mn.contains("_cast"), "has method _cast");
        check(mn.contains("_cdata"), "has method _cdata");
        check(mn.contains("_comment"), "has method _comment");
        check(mn.contains("_element"), "has method _element");
        check(mn.contains("_namespace"), "has method _namespace");
        check(mn.contains("_pcdata"), "has method _pcdata");
        check(mn.contains("block"), "has method block");
        check(mn.contains("commit"), "has method commit");
        check(mn.contains("getDocument"), "has method getDocument");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
