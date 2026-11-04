/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.Parser conformance: reflection.
 * @run main ParserTest
 */

public class ParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.Parser", false, ParserTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("Parser"), "simple name Parser");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.compiler.Constants"), "interface com.sun.org.apache.xalan.internal.xsltc.compiler.Constants");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.xml.sax.ContentHandler"), "interface org.xml.sax.ContentHandler");
        check(has(c, "init"), "method init");
        check(has(c, "setOutput"), "method setOutput");
        check(has(c, "getOutput"), "method getOutput");
        check(has(c, "getOutputProperties"), "method getOutputProperties");
        check(has(c, "addVariable"), "method addVariable");
        check(has(c, "addParameter"), "method addParameter");
        check(has(c, "removeVariable"), "method removeVariable");
        check(has(c, "lookupVariable"), "method lookupVariable");
        check(has(c, "setXSLTC"), "method setXSLTC");
        check(has(c, "getXSLTC"), "method getXSLTC");
        check(has(c, "getCurrentImportPrecedence"), "method getCurrentImportPrecedence");
        check(has(c, "getNextImportPrecedence"), "method getNextImportPrecedence");
        check(has(c, "setCurrentStylesheet"), "method setCurrentStylesheet");
        check(has(c, "getCurrentStylesheet"), "method getCurrentStylesheet");
        check(has(c, "getTopLevelStylesheet"), "method getTopLevelStylesheet");
        check(has(c, "getQNameSafe"), "method getQNameSafe");
        check(has(c, "getQName"), "method getQName");
        check(has(c, "getQNameIgnoreDefaultNs"), "method getQNameIgnoreDefaultNs");
        check(has(c, "getUseAttributeSets"), "method getUseAttributeSets");
        check(has(c, "getExtensionElementPrefixes"), "method getExtensionElementPrefixes");
        check(has(c, "getExcludeResultPrefixes"), "method getExcludeResultPrefixes");
        check(has(c, "makeStylesheet"), "method makeStylesheet");
        check(has(c, "createAST"), "method createAST");
        check(has(c, "parse"), "method parse");
        check(has(c, "getDocumentRoot"), "method getDocumentRoot");
        check(has(c, "elementSupported"), "method elementSupported");
        check(has(c, "functionSupported"), "method functionSupported");
        check(has(c, "getSymbolTable"), "method getSymbolTable");
        check(has(c, "getTemplate"), "method getTemplate");
        check(has(c, "setTemplate"), "method setTemplate");
        check(has(c, "getTemplateIndex"), "method getTemplateIndex");
        check(has(c, "makeInstance"), "method makeInstance");
        check(has(c, "parseExpression"), "method parseExpression");
        check(has(c, "parsePattern"), "method parsePattern");
        check(has(c, "errorsFound"), "method errorsFound");
        check(has(c, "printErrors"), "method printErrors");
        check(has(c, "printWarnings"), "method printWarnings");
        check(has(c, "reportError"), "method reportError");
        check(has(c, "getErrors"), "method getErrors");
        check(has(c, "getWarnings"), "method getWarnings");
        check(has(c, "startDocument"), "method startDocument");
        check(has(c, "endDocument"), "method endDocument");
        check(has(c, "startPrefixMapping"), "method startPrefixMapping");
        check(has(c, "endPrefixMapping"), "method endPrefixMapping");
        check(has(c, "startElement"), "method startElement");
        check(has(c, "endElement"), "method endElement");
        check(has(c, "characters"), "method characters");
        check(has(c, "processingInstruction"), "method processingInstruction");
        check(has(c, "ignorableWhitespace"), "method ignorableWhitespace");
        check(has(c, "skippedEntity"), "method skippedEntity");
        check(has(c, "setDocumentLocator"), "method setDocumentLocator");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
