/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.Stylesheet conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.compiler:+open
 * @run main StylesheetTest
 */
public class StylesheetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.Stylesheet", false, StylesheetTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.Stylesheet"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.SyntaxTreeNode"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 0, "interface-count");
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
        check(fd.size() == 6, "field-count");
        check(fd.contains("_importedFrom"), "field-_importedFrom");
        check(fd.contains("_includedFrom"), "field-_includedFrom");
        check(fd.contains("UNKNOWN_OUTPUT"), "field-UNKNOWN_OUTPUT");
        check(fd.contains("XML_OUTPUT"), "field-XML_OUTPUT");
        check(fd.contains("HTML_OUTPUT"), "field-HTML_OUTPUT");
        check(fd.contains("TEXT_OUTPUT"), "field-TEXT_OUTPUT");
        check(md.size() == 48, "method-count");
        check(md.contains("addIncludedStylesheet"), "method-addIncludedStylesheet");
        check(md.contains("addParam"), "method-addParam");
        check(md.contains("addPrefixMapping"), "method-addPrefixMapping");
        check(md.contains("addTemplate"), "method-addTemplate");
        check(md.contains("addVariable"), "method-addVariable");
        check(md.contains("callsNodeset"), "method-callsNodeset");
        check(md.contains("checkForLoop"), "method-checkForLoop");
        check(md.contains("declareExtensionPrefixes"), "method-declareExtensionPrefixes");
        check(md.contains("display"), "method-display");
        check(md.contains("getAllValidTemplates"), "method-getAllValidTemplates");
        check(md.contains("getClassName"), "method-getClassName");
        check(md.contains("getImportPrecedence"), "method-getImportPrecedence");
        check(md.contains("getLastOutputElement"), "method-getLastOutputElement");
        check(md.contains("getMinimumDescendantPrecedence"), "method-getMinimumDescendantPrecedence");
        check(md.contains("getMode"), "method-getMode");
        check(md.contains("getNamespace"), "method-getNamespace");
        check(md.contains("getOutputMethod"), "method-getOutputMethod");
        check(md.contains("getOutputProperties"), "method-getOutputProperties");
        check(md.contains("getParentStylesheet"), "method-getParentStylesheet");
        check(md.contains("getSourceLoader"), "method-getSourceLoader");
        check(md.contains("getSystemId"), "method-getSystemId");
        check(md.contains("getTemplateInlining"), "method-getTemplateInlining");
        check(md.contains("getTemplates"), "method-getTemplates");
        check(md.contains("hasGlobals"), "method-hasGlobals");
        check(md.contains("hasLocalParams"), "method-hasLocalParams");
        check(md.contains("isExtension"), "method-isExtension");
        check(md.contains("isMultiDocument"), "method-isMultiDocument");
        check(md.contains("isSimplified"), "method-isSimplified");
        check(md.contains("numberFormattingUsed"), "method-numberFormattingUsed");
        check(md.contains("parseContents"), "method-parseContents");
        check(md.contains("parseOwnChildren"), "method-parseOwnChildren");
        check(md.contains("processModes"), "method-processModes");
        check(md.contains("setCallsNodeset"), "method-setCallsNodeset");
        check(md.contains("setHasIdCall"), "method-setHasIdCall");
        check(md.contains("setImportPrecedence"), "method-setImportPrecedence");
        check(md.contains("setImportingStylesheet"), "method-setImportingStylesheet");
        check(md.contains("setIncludingStylesheet"), "method-setIncludingStylesheet");
        check(md.contains("setMultiDocument"), "method-setMultiDocument");
        check(md.contains("setOutputProperties"), "method-setOutputProperties");
        check(md.contains("setOutputProperty"), "method-setOutputProperty");
        check(md.contains("setParentStylesheet"), "method-setParentStylesheet");
        check(md.contains("setParser"), "method-setParser");
        check(md.contains("setSimplified"), "method-setSimplified");
        check(md.contains("setSourceLoader"), "method-setSourceLoader");
        check(md.contains("setSystemId"), "method-setSystemId");
        check(md.contains("setTemplateInlining"), "method-setTemplateInlining");
        check(md.contains("translate"), "method-translate");
        check(md.contains("typeCheck"), "method-typeCheck");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
