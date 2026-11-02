/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.Parser conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.compiler:+open
 * @run main ParserTest
 */
public class ParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.Parser", false, ParserTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.Parser"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 2, "interface-count");
        check(ifs.contains("com.sun.org.apache.xalan.internal.xsltc.compiler.Constants"), "if-com.sun.org.apache.xalan.internal.xsltc.compiler.Constants");
        check(ifs.contains("org.xml.sax.ContentHandler"), "if-org.xml.sax.ContentHandler");
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
        check(md.size() == 52, "method-count");
        check(md.contains("addParameter"), "method-addParameter");
        check(md.contains("addVariable"), "method-addVariable");
        check(md.contains("characters"), "method-characters");
        check(md.contains("createAST"), "method-createAST");
        check(md.contains("elementSupported"), "method-elementSupported");
        check(md.contains("endDocument"), "method-endDocument");
        check(md.contains("endElement"), "method-endElement");
        check(md.contains("endPrefixMapping"), "method-endPrefixMapping");
        check(md.contains("errorsFound"), "method-errorsFound");
        check(md.contains("functionSupported"), "method-functionSupported");
        check(md.contains("getCurrentImportPrecedence"), "method-getCurrentImportPrecedence");
        check(md.contains("getCurrentStylesheet"), "method-getCurrentStylesheet");
        check(md.contains("getDocumentRoot"), "method-getDocumentRoot");
        check(md.contains("getErrors"), "method-getErrors");
        check(md.contains("getExcludeResultPrefixes"), "method-getExcludeResultPrefixes");
        check(md.contains("getExtensionElementPrefixes"), "method-getExtensionElementPrefixes");
        check(md.contains("getNextImportPrecedence"), "method-getNextImportPrecedence");
        check(md.contains("getOutput"), "method-getOutput");
        check(md.contains("getOutputProperties"), "method-getOutputProperties");
        check(md.contains("getQName"), "method-getQName");
        check(md.contains("getQNameIgnoreDefaultNs"), "method-getQNameIgnoreDefaultNs");
        check(md.contains("getQNameSafe"), "method-getQNameSafe");
        check(md.contains("getSymbolTable"), "method-getSymbolTable");
        check(md.contains("getTemplate"), "method-getTemplate");
        check(md.contains("getTemplateIndex"), "method-getTemplateIndex");
        check(md.contains("getTopLevelStylesheet"), "method-getTopLevelStylesheet");
        check(md.contains("getUseAttributeSets"), "method-getUseAttributeSets");
        check(md.contains("getWarnings"), "method-getWarnings");
        check(md.contains("getXSLTC"), "method-getXSLTC");
        check(md.contains("ignorableWhitespace"), "method-ignorableWhitespace");
        check(md.contains("init"), "method-init");
        check(md.contains("lookupVariable"), "method-lookupVariable");
        check(md.contains("makeInstance"), "method-makeInstance");
        check(md.contains("makeStylesheet"), "method-makeStylesheet");
        check(md.contains("parse"), "method-parse");
        check(md.contains("parseExpression"), "method-parseExpression");
        check(md.contains("parsePattern"), "method-parsePattern");
        check(md.contains("printErrors"), "method-printErrors");
        check(md.contains("printWarnings"), "method-printWarnings");
        check(md.contains("processingInstruction"), "method-processingInstruction");
        check(md.contains("removeVariable"), "method-removeVariable");
        check(md.contains("reportError"), "method-reportError");
        check(md.contains("setCurrentStylesheet"), "method-setCurrentStylesheet");
        check(md.contains("setDocumentLocator"), "method-setDocumentLocator");
        check(md.contains("setOutput"), "method-setOutput");
        check(md.contains("setPIParameters"), "method-setPIParameters");
        check(md.contains("setTemplate"), "method-setTemplate");
        check(md.contains("setXSLTC"), "method-setXSLTC");
        check(md.contains("skippedEntity"), "method-skippedEntity");
        check(md.contains("startDocument"), "method-startDocument");
        check(md.contains("startElement"), "method-startElement");
        check(md.contains("startPrefixMapping"), "method-startPrefixMapping");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
