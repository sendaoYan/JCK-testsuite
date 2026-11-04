/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.runtime:+open
 * @run main BasisLibraryTest
 */
public class BasisLibraryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary", false, BasisLibraryTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary"), "name");
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
        check(fd.size() == 25, "field-count");
        check(fd.contains("RUN_TIME_INTERNAL_ERR"), "field-RUN_TIME_INTERNAL_ERR");
        check(fd.contains("RUN_TIME_COPY_ERR"), "field-RUN_TIME_COPY_ERR");
        check(fd.contains("DATA_CONVERSION_ERR"), "field-DATA_CONVERSION_ERR");
        check(fd.contains("EXTERNAL_FUNC_ERR"), "field-EXTERNAL_FUNC_ERR");
        check(fd.contains("EQUALITY_EXPR_ERR"), "field-EQUALITY_EXPR_ERR");
        check(fd.contains("INVALID_ARGUMENT_ERR"), "field-INVALID_ARGUMENT_ERR");
        check(fd.contains("FORMAT_NUMBER_ERR"), "field-FORMAT_NUMBER_ERR");
        check(fd.contains("ITERATOR_CLONE_ERR"), "field-ITERATOR_CLONE_ERR");
        check(fd.contains("AXIS_SUPPORT_ERR"), "field-AXIS_SUPPORT_ERR");
        check(fd.contains("TYPED_AXIS_SUPPORT_ERR"), "field-TYPED_AXIS_SUPPORT_ERR");
        check(fd.contains("STRAY_ATTRIBUTE_ERR"), "field-STRAY_ATTRIBUTE_ERR");
        check(fd.contains("STRAY_NAMESPACE_ERR"), "field-STRAY_NAMESPACE_ERR");
        check(fd.contains("NAMESPACE_PREFIX_ERR"), "field-NAMESPACE_PREFIX_ERR");
        check(fd.contains("DOM_ADAPTER_INIT_ERR"), "field-DOM_ADAPTER_INIT_ERR");
        check(fd.contains("PARSER_DTD_SUPPORT_ERR"), "field-PARSER_DTD_SUPPORT_ERR");
        check(fd.contains("NAMESPACES_SUPPORT_ERR"), "field-NAMESPACES_SUPPORT_ERR");
        check(fd.contains("CANT_RESOLVE_RELATIVE_URI_ERR"), "field-CANT_RESOLVE_RELATIVE_URI_ERR");
        check(fd.contains("UNSUPPORTED_XSL_ERR"), "field-UNSUPPORTED_XSL_ERR");
        check(fd.contains("UNSUPPORTED_EXT_ERR"), "field-UNSUPPORTED_EXT_ERR");
        check(fd.contains("UNKNOWN_TRANSLET_VERSION_ERR"), "field-UNKNOWN_TRANSLET_VERSION_ERR");
        check(fd.contains("INVALID_QNAME_ERR"), "field-INVALID_QNAME_ERR");
        check(fd.contains("INVALID_NCNAME_ERR"), "field-INVALID_NCNAME_ERR");
        check(fd.contains("UNALLOWED_EXTENSION_FUNCTION_ERR"), "field-UNALLOWED_EXTENSION_FUNCTION_ERR");
        check(fd.contains("UNALLOWED_EXTENSION_ELEMENT_ERR"), "field-UNALLOWED_EXTENSION_ELEMENT_ERR");
        check(fd.contains("ERROR_MESSAGES_KEY"), "field-ERROR_MESSAGES_KEY");
        check(md.size() == 53, "method-count");
        check(md.contains("booleanF"), "method-booleanF");
        check(md.contains("checkAttribQName"), "method-checkAttribQName");
        check(md.contains("checkNCName"), "method-checkNCName");
        check(md.contains("checkQName"), "method-checkQName");
        check(md.contains("compare"), "method-compare");
        check(md.contains("consoleOutput"), "method-consoleOutput");
        check(md.contains("copy"), "method-copy");
        check(md.contains("countF"), "method-countF");
        check(md.contains("formatNumber"), "method-formatNumber");
        check(md.contains("generatePrefix"), "method-generatePrefix");
        check(md.contains("generate_idF"), "method-generate_idF");
        check(md.contains("getLocalName"), "method-getLocalName");
        check(md.contains("getPrefix"), "method-getPrefix");
        check(md.contains("getSingleNode"), "method-getSingleNode");
        check(md.contains("getStringLength"), "method-getStringLength");
        check(md.contains("mapQNameToJavaName"), "method-mapQNameToJavaName");
        check(md.contains("namespace_uriF"), "method-namespace_uriF");
        check(md.contains("node2Iterator"), "method-node2Iterator");
        check(md.contains("nodeList2Iterator"), "method-nodeList2Iterator");
        check(md.contains("nodesetF"), "method-nodesetF");
        check(md.contains("normalize_spaceF"), "method-normalize_spaceF");
        check(md.contains("numberF"), "method-numberF");
        check(md.contains("objectTypeF"), "method-objectTypeF");
        check(md.contains("positionF"), "method-positionF");
        check(md.contains("realToInt"), "method-realToInt");
        check(md.contains("realToString"), "method-realToString");
        check(md.contains("referenceToBoolean"), "method-referenceToBoolean");
        check(md.contains("referenceToDouble"), "method-referenceToDouble");
        check(md.contains("referenceToLong"), "method-referenceToLong");
        check(md.contains("referenceToNode"), "method-referenceToNode");
        check(md.contains("referenceToNodeList"), "method-referenceToNodeList");
        check(md.contains("referenceToNodeSet"), "method-referenceToNodeSet");
        check(md.contains("referenceToResultTree"), "method-referenceToResultTree");
        check(md.contains("referenceToString"), "method-referenceToString");
        check(md.contains("replace"), "method-replace");
        check(md.contains("resetPrefixIndex"), "method-resetPrefixIndex");
        check(md.contains("roundF"), "method-roundF");
        check(md.contains("runTimeError"), "method-runTimeError");
        check(md.contains("startXslElement"), "method-startXslElement");
        check(md.contains("stringF"), "method-stringF");
        check(md.contains("stringToInt"), "method-stringToInt");
        check(md.contains("stringToReal"), "method-stringToReal");
        check(md.contains("substringF"), "method-substringF");
        check(md.contains("substring_afterF"), "method-substring_afterF");
        check(md.contains("substring_beforeF"), "method-substring_beforeF");
        check(md.contains("sumF"), "method-sumF");
        check(md.contains("system_propertyF"), "method-system_propertyF");
        check(md.contains("testLanguage"), "method-testLanguage");
        check(md.contains("translateF"), "method-translateF");
        check(md.contains("unallowed_extension_elementF"), "method-unallowed_extension_elementF");
        check(md.contains("unallowed_extension_functionF"), "method-unallowed_extension_functionF");
        check(md.contains("unresolved_externalF"), "method-unresolved_externalF");
        check(md.contains("unsupported_ElementF"), "method-unsupported_ElementF");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
