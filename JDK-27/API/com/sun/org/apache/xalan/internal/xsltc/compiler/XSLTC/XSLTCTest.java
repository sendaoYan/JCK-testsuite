/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.compiler.XSLTC conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.compiler:+open
 * @run main XSLTCTest
 */
public class XSLTCTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.compiler.XSLTC", false, XSLTCTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.compiler.XSLTC"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
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
        check(fd.size() == 5, "field-count");
        check(fd.contains("JAR_OUTPUT"), "field-JAR_OUTPUT");
        check(fd.contains("BYTEARRAY_OUTPUT"), "field-BYTEARRAY_OUTPUT");
        check(fd.contains("CLASSLOADER_OUTPUT"), "field-CLASSLOADER_OUTPUT");
        check(fd.contains("BYTEARRAY_AND_FILE_OUTPUT"), "field-BYTEARRAY_AND_FILE_OUTPUT");
        check(fd.contains("BYTEARRAY_AND_JAR_OUTPUT"), "field-BYTEARRAY_AND_JAR_OUTPUT");
        check(md.size() == 56, "method-count");
        check(md.contains("addCharacterData"), "method-addCharacterData");
        check(md.contains("callsNodeset"), "method-callsNodeset");
        check(md.contains("compile"), "method-compile");
        check(md.contains("debug"), "method-debug");
        check(md.contains("dumpClass"), "method-dumpClass");
        check(md.contains("getBytecodes"), "method-getBytecodes");
        check(md.contains("getCharacterData"), "method-getCharacterData");
        check(md.contains("getCharacterDataCount"), "method-getCharacterDataCount");
        check(md.contains("getClassName"), "method-getClassName");
        check(md.contains("getErrors"), "method-getErrors");
        check(md.contains("getExternalExtensionFunctions"), "method-getExternalExtensionFunctions");
        check(md.contains("getFeature"), "method-getFeature");
        check(md.contains("getHelperClassName"), "method-getHelperClassName");
        check(md.contains("getJarFileName"), "method-getJarFileName");
        check(md.contains("getNamesIndex"), "method-getNamesIndex");
        check(md.contains("getNamespaceIndex"), "method-getNamespaceIndex");
        check(md.contains("getNumberFieldIndexes"), "method-getNumberFieldIndexes");
        check(md.contains("getOutputProperties"), "method-getOutputProperties");
        check(md.contains("getParser"), "method-getParser");
        check(md.contains("getProperty"), "method-getProperty");
        check(md.contains("getStylesheet"), "method-getStylesheet");
        check(md.contains("getTemplateInlining"), "method-getTemplateInlining");
        check(md.contains("getWarnings"), "method-getWarnings");
        check(md.contains("getXMLReader"), "method-getXMLReader");
        check(md.contains("hasIdCall"), "method-hasIdCall");
        check(md.contains("init"), "method-init");
        check(md.contains("isMultiDocument"), "method-isMultiDocument");
        check(md.contains("isSecureProcessing"), "method-isSecureProcessing");
        check(md.contains("nextAttributeSetSerial"), "method-nextAttributeSetSerial");
        check(md.contains("nextHelperClassSerial"), "method-nextHelperClassSerial");
        check(md.contains("nextModeSerial"), "method-nextModeSerial");
        check(md.contains("nextStepPatternSerial"), "method-nextStepPatternSerial");
        check(md.contains("nextStylesheetSerial"), "method-nextStylesheetSerial");
        check(md.contains("outputToJar"), "method-outputToJar");
        check(md.contains("printErrors"), "method-printErrors");
        check(md.contains("printWarnings"), "method-printWarnings");
        check(md.contains("registerAttribute"), "method-registerAttribute");
        check(md.contains("registerElement"), "method-registerElement");
        check(md.contains("registerNamespace"), "method-registerNamespace");
        check(md.contains("registerNamespacePrefix"), "method-registerNamespacePrefix");
        check(md.contains("setCallsNodeset"), "method-setCallsNodeset");
        check(md.contains("setClassName"), "method-setClassName");
        check(md.contains("setDebug"), "method-setDebug");
        check(md.contains("setDestDirectory"), "method-setDestDirectory");
        check(md.contains("setHasIdCall"), "method-setHasIdCall");
        check(md.contains("setJarFileName"), "method-setJarFileName");
        check(md.contains("setMultiDocument"), "method-setMultiDocument");
        check(md.contains("setOutputType"), "method-setOutputType");
        check(md.contains("setPIParameters"), "method-setPIParameters");
        check(md.contains("setPackageName"), "method-setPackageName");
        check(md.contains("setProperty"), "method-setProperty");
        check(md.contains("setSecureProcessing"), "method-setSecureProcessing");
        check(md.contains("setSourceLoader"), "method-setSourceLoader");
        check(md.contains("setStylesheet"), "method-setStylesheet");
        check(md.contains("setTemplateInlining"), "method-setTemplateInlining");
        check(md.contains("setXMLReader"), "method-setXMLReader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
