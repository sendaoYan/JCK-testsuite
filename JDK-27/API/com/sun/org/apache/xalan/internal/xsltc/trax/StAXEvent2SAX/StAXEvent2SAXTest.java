/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.StAXEvent2SAX conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.trax:+open
 * @run main StAXEvent2SAXTest
 */
public class StAXEvent2SAXTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.StAXEvent2SAX", false, StAXEvent2SAXTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.trax.StAXEvent2SAX"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "interface-count");
        check(ifs.contains("org.xml.sax.XMLReader"), "if-org.xml.sax.XMLReader");
        check(ifs.contains("org.xml.sax.Locator"), "if-org.xml.sax.Locator");
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
        check(fd.size() == 0, "field-count");
        check(md.size() == 17, "method-count");
        check(md.contains("getColumnNumber"), "method-getColumnNumber");
        check(md.contains("getContentHandler"), "method-getContentHandler");
        check(md.contains("getDTDHandler"), "method-getDTDHandler");
        check(md.contains("getEntityResolver"), "method-getEntityResolver");
        check(md.contains("getErrorHandler"), "method-getErrorHandler");
        check(md.contains("getFeature"), "method-getFeature");
        check(md.contains("getLineNumber"), "method-getLineNumber");
        check(md.contains("getProperty"), "method-getProperty");
        check(md.contains("getPublicId"), "method-getPublicId");
        check(md.contains("getSystemId"), "method-getSystemId");
        check(md.contains("parse"), "method-parse");
        check(md.contains("setContentHandler"), "method-setContentHandler");
        check(md.contains("setDTDHandler"), "method-setDTDHandler");
        check(md.contains("setEntityResolver"), "method-setEntityResolver");
        check(md.contains("setErrorHandler"), "method-setErrorHandler");
        check(md.contains("setFeature"), "method-setFeature");
        check(md.contains("setProperty"), "method-setProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
