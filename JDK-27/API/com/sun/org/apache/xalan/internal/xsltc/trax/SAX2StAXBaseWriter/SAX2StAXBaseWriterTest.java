/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.trax:+open
 * @run main SAX2StAXBaseWriterTest
 */
public class SAX2StAXBaseWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter", false, SAX2StAXBaseWriterTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.xml.sax.helpers.DefaultHandler"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("org.xml.sax.ext.LexicalHandler"), "if-org.xml.sax.ext.LexicalHandler");
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
        check(fd.size() == 5, "field-count");
        check(fd.contains("isCDATA"), "field-isCDATA");
        check(fd.contains("CDATABuffer"), "field-CDATABuffer");
        check(fd.contains("namespaces"), "field-namespaces");
        check(fd.contains("docLocator"), "field-docLocator");
        check(fd.contains("reporter"), "field-reporter");
        check(md.size() == 24, "method-count");
        check(md.contains("characters"), "method-characters");
        check(md.contains("comment"), "method-comment");
        check(md.contains("endCDATA"), "method-endCDATA");
        check(md.contains("endDTD"), "method-endDTD");
        check(md.contains("endDocument"), "method-endDocument");
        check(md.contains("endElement"), "method-endElement");
        check(md.contains("endEntity"), "method-endEntity");
        check(md.contains("endPrefixMapping"), "method-endPrefixMapping");
        check(md.contains("error"), "method-error");
        check(md.contains("fatalError"), "method-fatalError");
        check(md.contains("getCurrentLocation"), "method-getCurrentLocation");
        check(md.contains("parseQName"), "method-parseQName");
        check(md.contains("reportException"), "method-reportException");
        check(md.contains("setDocumentLocator"), "method-setDocumentLocator");
        check(md.contains("setEncoding"), "method-setEncoding");
        check(md.contains("setXMLReporter"), "method-setXMLReporter");
        check(md.contains("setXmlVersion"), "method-setXmlVersion");
        check(md.contains("startCDATA"), "method-startCDATA");
        check(md.contains("startDTD"), "method-startDTD");
        check(md.contains("startDocument"), "method-startDocument");
        check(md.contains("startElement"), "method-startElement");
        check(md.contains("startEntity"), "method-startEntity");
        check(md.contains("startPrefixMapping"), "method-startPrefixMapping");
        check(md.contains("warning"), "method-warning");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
