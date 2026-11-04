/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.runtime:+open
 * @run main AbstractTransletTest
 */
public class AbstractTransletTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet", false, AbstractTransletTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("com.sun.org.apache.xalan.internal.xsltc.Translet"), "if-com.sun.org.apache.xalan.internal.xsltc.Translet");
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
        check(fd.size() == 28, "field-count");
        check(fd.contains("_version"), "field-_version");
        check(fd.contains("_method"), "field-_method");
        check(fd.contains("_encoding"), "field-_encoding");
        check(fd.contains("_omitHeader"), "field-_omitHeader");
        check(fd.contains("_standalone"), "field-_standalone");
        check(fd.contains("_isStandalone"), "field-_isStandalone");
        check(fd.contains("_doctypePublic"), "field-_doctypePublic");
        check(fd.contains("_doctypeSystem"), "field-_doctypeSystem");
        check(fd.contains("_indent"), "field-_indent");
        check(fd.contains("_mediaType"), "field-_mediaType");
        check(fd.contains("_cdata"), "field-_cdata");
        check(fd.contains("_indentamount"), "field-_indentamount");
        check(fd.contains("FIRST_TRANSLET_VERSION"), "field-FIRST_TRANSLET_VERSION");
        check(fd.contains("VER_SPLIT_NAMES_ARRAY"), "field-VER_SPLIT_NAMES_ARRAY");
        check(fd.contains("CURRENT_TRANSLET_VERSION"), "field-CURRENT_TRANSLET_VERSION");
        check(fd.contains("transletVersion"), "field-transletVersion");
        check(fd.contains("namesArray"), "field-namesArray");
        check(fd.contains("urisArray"), "field-urisArray");
        check(fd.contains("typesArray"), "field-typesArray");
        check(fd.contains("namespaceArray"), "field-namespaceArray");
        check(fd.contains("_templates"), "field-_templates");
        check(fd.contains("_hasIdCall"), "field-_hasIdCall");
        check(fd.contains("stringValueHandler"), "field-stringValueHandler");
        check(fd.contains("pbase"), "field-pbase");
        check(fd.contains("pframe"), "field-pframe");
        check(fd.contains("paramsStack"), "field-paramsStack");
        check(fd.contains("_formatSymbols"), "field-_formatSymbols");
        check(fd.contains("_domImplementation"), "field-_domImplementation");
        check(md.size() == 44, "method-count");
        check(md.contains("addAuxiliaryClass"), "method-addAuxiliaryClass");
        check(md.contains("addCdataElement"), "method-addCdataElement");
        check(md.contains("addDecimalFormat"), "method-addDecimalFormat");
        check(md.contains("addParameter"), "method-addParameter");
        check(md.contains("buildKeyIndex"), "method-buildKeyIndex");
        check(md.contains("buildKeys"), "method-buildKeys");
        check(md.contains("characters"), "method-characters");
        check(md.contains("clearParameters"), "method-clearParameters");
        check(md.contains("closeOutputHandler"), "method-closeOutputHandler");
        check(md.contains("createKeyIndex"), "method-createKeyIndex");
        check(md.contains("displayMessage"), "method-displayMessage");
        check(md.contains("getAccessError"), "method-getAccessError");
        check(md.contains("getAllowedProtocols"), "method-getAllowedProtocols");
        check(md.contains("getAuxiliaryClass"), "method-getAuxiliaryClass");
        check(md.contains("getDOMCache"), "method-getDOMCache");
        check(md.contains("getDecimalFormat"), "method-getDecimalFormat");
        check(md.contains("getKeyIndex"), "method-getKeyIndex");
        check(md.contains("getNamesArray"), "method-getNamesArray");
        check(md.contains("getNamespaceArray"), "method-getNamespaceArray");
        check(md.contains("getParameter"), "method-getParameter");
        check(md.contains("getTemplates"), "method-getTemplates");
        check(md.contains("getTypesArray"), "method-getTypesArray");
        check(md.contains("getUrisArray"), "method-getUrisArray");
        check(md.contains("hasIdCall"), "method-hasIdCall");
        check(md.contains("makeDOMAdapter"), "method-makeDOMAdapter");
        check(md.contains("newDocument"), "method-newDocument");
        check(md.contains("openOutputHandler"), "method-openOutputHandler");
        check(md.contains("overrideDefaultParser"), "method-overrideDefaultParser");
        check(md.contains("popParamFrame"), "method-popParamFrame");
        check(md.contains("postInitialization"), "method-postInitialization");
        check(md.contains("prepassDocument"), "method-prepassDocument");
        check(md.contains("printInternalState"), "method-printInternalState");
        check(md.contains("pushParamFrame"), "method-pushParamFrame");
        check(md.contains("setAccessError"), "method-setAccessError");
        check(md.contains("setAllowedProtocols"), "method-setAllowedProtocols");
        check(md.contains("setAuxiliaryClasses"), "method-setAuxiliaryClasses");
        check(md.contains("setDOMCache"), "method-setDOMCache");
        check(md.contains("setIndexSize"), "method-setIndexSize");
        check(md.contains("setKeyIndexDom"), "method-setKeyIndexDom");
        check(md.contains("setMessageHandler"), "method-setMessageHandler");
        check(md.contains("setOverrideDefaultParser"), "method-setOverrideDefaultParser");
        check(md.contains("setTemplates"), "method-setTemplates");
        check(md.contains("transferOutputSettings"), "method-transferOutputSettings");
        check(md.contains("transform"), "method-transform");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
