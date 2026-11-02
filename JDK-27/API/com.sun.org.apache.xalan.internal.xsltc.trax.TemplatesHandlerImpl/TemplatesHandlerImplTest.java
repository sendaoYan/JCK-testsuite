/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesHandlerImpl conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.trax:+open
 * @run main TemplatesHandlerImplTest
 */
public class TemplatesHandlerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesHandlerImpl", false, TemplatesHandlerImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesHandlerImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 3, "interface-count");
        check(ifs.contains("org.xml.sax.ContentHandler"), "if-org.xml.sax.ContentHandler");
        check(ifs.contains("javax.xml.transform.sax.TemplatesHandler"), "if-javax.xml.transform.sax.TemplatesHandler");
        check(ifs.contains("com.sun.org.apache.xalan.internal.xsltc.compiler.SourceLoader"), "if-com.sun.org.apache.xalan.internal.xsltc.compiler.SourceLoader");
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
        check(md.size() == 16, "method-count");
        check(md.contains("characters"), "method-characters");
        check(md.contains("endDocument"), "method-endDocument");
        check(md.contains("endElement"), "method-endElement");
        check(md.contains("endPrefixMapping"), "method-endPrefixMapping");
        check(md.contains("getSystemId"), "method-getSystemId");
        check(md.contains("getTemplates"), "method-getTemplates");
        check(md.contains("ignorableWhitespace"), "method-ignorableWhitespace");
        check(md.contains("loadSource"), "method-loadSource");
        check(md.contains("processingInstruction"), "method-processingInstruction");
        check(md.contains("setDocumentLocator"), "method-setDocumentLocator");
        check(md.contains("setSystemId"), "method-setSystemId");
        check(md.contains("setURIResolver"), "method-setURIResolver");
        check(md.contains("skippedEntity"), "method-skippedEntity");
        check(md.contains("startDocument"), "method-startDocument");
        check(md.contains("startElement"), "method-startElement");
        check(md.contains("startPrefixMapping"), "method-startPrefixMapping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
