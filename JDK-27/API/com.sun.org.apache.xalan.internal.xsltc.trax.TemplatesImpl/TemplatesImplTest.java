/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.trax:+open
 * @run main TemplatesImplTest
 */
public class TemplatesImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl", false, TemplatesImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "interface-count");
        check(ifs.contains("javax.xml.transform.Templates"), "if-javax.xml.transform.Templates");
        check(ifs.contains("java.io.Serializable"), "if-java.io.Serializable");
        java.util.Set<String> ec = new java.util.HashSet<>();
        java.util.Set<String> fd = new java.util.HashSet<>();
        java.util.Set<String> md = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (f.isEnumConstant()) ec.add(f.getName());
            else if (java.lang.reflect.Modifier.isPublic(f.getModifiers()) || java.lang.reflect.Modifier.isProtected(f.getModifiers())) fd.add(f.getName());
        }
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers()) || java.lang.reflect.Modifier.isProtected(m.getModifiers())) md.add(m.getName());
        }
        check(ec.size() == 0, "enum-constant-count");
        check(fd.size() == 1, "field-count");
        check(fd.contains("DESERIALIZE_TRANSLET"), "field-DESERIALIZE_TRANSLET");
        check(md.size() == 9, "method-count");
        check(md.contains("getOutputProperties"), "method-getOutputProperties");
        check(md.contains("getStylesheetDOM"), "method-getStylesheetDOM");
        check(md.contains("getTransletIndex"), "method-getTransletIndex");
        check(md.contains("getTransletName"), "method-getTransletName");
        check(md.contains("newTransformer"), "method-newTransformer");
        check(md.contains("overrideDefaultParser"), "method-overrideDefaultParser");
        check(md.contains("setStylesheetDOM"), "method-setStylesheetDOM");
        check(md.contains("setTransletName"), "method-setTransletName");
        check(md.contains("setURIResolver"), "method-setURIResolver");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
