/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.TransformerHandlerImpl conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.trax:+open
 * @run main TransformerHandlerImplTest
 */
public class TransformerHandlerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerHandlerImpl", false, TransformerHandlerImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerHandlerImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "interface-count");
        check(ifs.contains("javax.xml.transform.sax.TransformerHandler"), "if-javax.xml.transform.sax.TransformerHandler");
        check(ifs.contains("org.xml.sax.ext.DeclHandler"), "if-org.xml.sax.ext.DeclHandler");
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
        check(md.size() == 29, "method-count");
        check(md.contains("attributeDecl"), "method-attributeDecl");
        check(md.contains("characters"), "method-characters");
        check(md.contains("comment"), "method-comment");
        check(md.contains("elementDecl"), "method-elementDecl");
        check(md.contains("endCDATA"), "method-endCDATA");
        check(md.contains("endDTD"), "method-endDTD");
        check(md.contains("endDocument"), "method-endDocument");
        check(md.contains("endElement"), "method-endElement");
        check(md.contains("endEntity"), "method-endEntity");
        check(md.contains("endPrefixMapping"), "method-endPrefixMapping");
        check(md.contains("externalEntityDecl"), "method-externalEntityDecl");
        check(md.contains("getSystemId"), "method-getSystemId");
        check(md.contains("getTransformer"), "method-getTransformer");
        check(md.contains("ignorableWhitespace"), "method-ignorableWhitespace");
        check(md.contains("internalEntityDecl"), "method-internalEntityDecl");
        check(md.contains("notationDecl"), "method-notationDecl");
        check(md.contains("processingInstruction"), "method-processingInstruction");
        check(md.contains("reset"), "method-reset");
        check(md.contains("setDocumentLocator"), "method-setDocumentLocator");
        check(md.contains("setResult"), "method-setResult");
        check(md.contains("setSystemId"), "method-setSystemId");
        check(md.contains("skippedEntity"), "method-skippedEntity");
        check(md.contains("startCDATA"), "method-startCDATA");
        check(md.contains("startDTD"), "method-startDTD");
        check(md.contains("startDocument"), "method-startDocument");
        check(md.contains("startElement"), "method-startElement");
        check(md.contains("startEntity"), "method-startEntity");
        check(md.contains("startPrefixMapping"), "method-startPrefixMapping");
        check(md.contains("unparsedEntityDecl"), "method-unparsedEntityDecl");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
