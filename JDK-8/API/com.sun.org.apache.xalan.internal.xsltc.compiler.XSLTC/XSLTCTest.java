/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.XSLTC conformance: reflection.
 * @run main XSLTCTest
 */

public class XSLTCTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.XSLTC", false, XSLTCTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("XSLTC"), "simple name XSLTC");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "setSecureProcessing"), "method setSecureProcessing");
        check(has(c, "isSecureProcessing"), "method isSecureProcessing");
        check(has(c, "getFeature"), "method getFeature");
        check(has(c, "getProperty"), "method getProperty");
        check(has(c, "setProperty"), "method setProperty");
        check(has(c, "getParser"), "method getParser");
        check(has(c, "setOutputType"), "method setOutputType");
        check(has(c, "getOutputProperties"), "method getOutputProperties");
        check(has(c, "init"), "method init");
        check(has(c, "getExternalExtensionFunctions"), "method getExternalExtensionFunctions");
        check(has(c, "setSourceLoader"), "method setSourceLoader");
        check(has(c, "setTemplateInlining"), "method setTemplateInlining");
        check(has(c, "getTemplateInlining"), "method getTemplateInlining");
        check(has(c, "setPIParameters"), "method setPIParameters");
        check(has(c, "compile"), "method compile");
        check(has(c, "getBytecodes"), "method getBytecodes");
        check(has(c, "setXMLReader"), "method setXMLReader");
        check(has(c, "getXMLReader"), "method getXMLReader");
        check(has(c, "getErrors"), "method getErrors");
        check(has(c, "getWarnings"), "method getWarnings");
        check(has(c, "printErrors"), "method printErrors");
        check(has(c, "printWarnings"), "method printWarnings");
        check(has(c, "isMultiDocument"), "method isMultiDocument");
        check(has(c, "callsNodeset"), "method callsNodeset");
        check(has(c, "hasIdCall"), "method hasIdCall");
        check(has(c, "setClassName"), "method setClassName");
        check(has(c, "getClassName"), "method getClassName");
        check(has(c, "setDestDirectory"), "method setDestDirectory");
        check(has(c, "setPackageName"), "method setPackageName");
        check(has(c, "setJarFileName"), "method setJarFileName");
        check(has(c, "getJarFileName"), "method getJarFileName");
        check(has(c, "setStylesheet"), "method setStylesheet");
        check(has(c, "getStylesheet"), "method getStylesheet");
        check(has(c, "registerAttribute"), "method registerAttribute");
        check(has(c, "registerElement"), "method registerElement");
        check(has(c, "registerNamespacePrefix"), "method registerNamespacePrefix");
        check(has(c, "registerNamespace"), "method registerNamespace");
        check(has(c, "nextModeSerial"), "method nextModeSerial");
        check(has(c, "nextStylesheetSerial"), "method nextStylesheetSerial");
        check(has(c, "nextStepPatternSerial"), "method nextStepPatternSerial");
        check(has(c, "getNumberFieldIndexes"), "method getNumberFieldIndexes");
        check(has(c, "nextHelperClassSerial"), "method nextHelperClassSerial");
        check(has(c, "nextAttributeSetSerial"), "method nextAttributeSetSerial");
        check(has(c, "getNamesIndex"), "method getNamesIndex");
        check(has(c, "getNamespaceIndex"), "method getNamespaceIndex");
        check(has(c, "getHelperClassName"), "method getHelperClassName");
        check(has(c, "dumpClass"), "method dumpClass");
        check(has(c, "outputToJar"), "method outputToJar");
        check(has(c, "setDebug"), "method setDebug");
        check(has(c, "debug"), "method debug");
        check(has(c, "getCharacterData"), "method getCharacterData");
        check(has(c, "getCharacterDataCount"), "method getCharacterDataCount");
        check(has(c, "addCharacterData"), "method addCharacterData");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
