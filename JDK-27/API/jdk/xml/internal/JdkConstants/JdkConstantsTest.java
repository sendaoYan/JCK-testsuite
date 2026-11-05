/*
 * @test
 * @summary jdk.xml.internal.JdkConstants conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main JdkConstantsTest
 */
public class JdkConstantsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkConstants", false, JdkConstantsTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.JdkConstants"), "name");
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
        check(fd.size() == 66, "field-count");
        check(fd.contains("SECURITY_MANAGER"), "field-SECURITY_MANAGER");
        check(fd.contains("ORACLE_JAXP_PROPERTY_PREFIX"), "field-ORACLE_JAXP_PROPERTY_PREFIX");
        check(fd.contains("JDK_ENTITY_EXPANSION_LIMIT"), "field-JDK_ENTITY_EXPANSION_LIMIT");
        check(fd.contains("JDK_ELEMENT_ATTRIBUTE_LIMIT"), "field-JDK_ELEMENT_ATTRIBUTE_LIMIT");
        check(fd.contains("JDK_MAX_OCCUR_LIMIT"), "field-JDK_MAX_OCCUR_LIMIT");
        check(fd.contains("JDK_TOTAL_ENTITY_SIZE_LIMIT"), "field-JDK_TOTAL_ENTITY_SIZE_LIMIT");
        check(fd.contains("JDK_GENERAL_ENTITY_SIZE_LIMIT"), "field-JDK_GENERAL_ENTITY_SIZE_LIMIT");
        check(fd.contains("JDK_ENTITY_REPLACEMENT_LIMIT"), "field-JDK_ENTITY_REPLACEMENT_LIMIT");
        check(fd.contains("JDK_PARAMETER_ENTITY_SIZE_LIMIT"), "field-JDK_PARAMETER_ENTITY_SIZE_LIMIT");
        check(fd.contains("JDK_XML_NAME_LIMIT"), "field-JDK_XML_NAME_LIMIT");
        check(fd.contains("JDK_MAX_ELEMENT_DEPTH"), "field-JDK_MAX_ELEMENT_DEPTH");
        check(fd.contains("JDK_ENTITY_COUNT_INFO"), "field-JDK_ENTITY_COUNT_INFO");
        check(fd.contains("JDK_DEBUG_LIMIT"), "field-JDK_DEBUG_LIMIT");
        check(fd.contains("SP_ENTITY_EXPANSION_LIMIT"), "field-SP_ENTITY_EXPANSION_LIMIT");
        check(fd.contains("SP_ELEMENT_ATTRIBUTE_LIMIT"), "field-SP_ELEMENT_ATTRIBUTE_LIMIT");
        check(fd.contains("SP_MAX_OCCUR_LIMIT"), "field-SP_MAX_OCCUR_LIMIT");
        check(fd.contains("SP_TOTAL_ENTITY_SIZE_LIMIT"), "field-SP_TOTAL_ENTITY_SIZE_LIMIT");
        check(fd.contains("SP_GENERAL_ENTITY_SIZE_LIMIT"), "field-SP_GENERAL_ENTITY_SIZE_LIMIT");
        check(fd.contains("SP_ENTITY_REPLACEMENT_LIMIT"), "field-SP_ENTITY_REPLACEMENT_LIMIT");
        check(fd.contains("SP_PARAMETER_ENTITY_SIZE_LIMIT"), "field-SP_PARAMETER_ENTITY_SIZE_LIMIT");
        check(fd.contains("SP_XML_NAME_LIMIT"), "field-SP_XML_NAME_LIMIT");
        check(fd.contains("SP_MAX_ELEMENT_DEPTH"), "field-SP_MAX_ELEMENT_DEPTH");
        check(fd.contains("XPATH_GROUP_LIMIT"), "field-XPATH_GROUP_LIMIT");
        check(fd.contains("XPATH_OP_LIMIT"), "field-XPATH_OP_LIMIT");
        check(fd.contains("XPATH_TOTALOP_LIMIT"), "field-XPATH_TOTALOP_LIMIT");
        check(fd.contains("XPATH_ALLOW_NON_XSLT_SYS_PROPS"), "field-XPATH_ALLOW_NON_XSLT_SYS_PROPS");
        check(fd.contains("JDK_EXTENSION_CLASSLOADER"), "field-JDK_EXTENSION_CLASSLOADER");
        check(fd.contains("JDK_EXT_CLASSLOADER"), "field-JDK_EXT_CLASSLOADER");
        check(fd.contains("ENTITY_EXPANSION_LIMIT"), "field-ENTITY_EXPANSION_LIMIT");
        check(fd.contains("ELEMENT_ATTRIBUTE_LIMIT"), "field-ELEMENT_ATTRIBUTE_LIMIT");
        check(fd.contains("MAX_OCCUR_LIMIT"), "field-MAX_OCCUR_LIMIT");
        check(fd.contains("JDK_YES"), "field-JDK_YES");
        check(fd.contains("ORACLE_FEATURE_SERVICE_MECHANISM"), "field-ORACLE_FEATURE_SERVICE_MECHANISM");
        check(fd.contains("SP_ACCESS_EXTERNAL_STYLESHEET"), "field-SP_ACCESS_EXTERNAL_STYLESHEET");
        check(fd.contains("SP_ACCESS_EXTERNAL_DTD"), "field-SP_ACCESS_EXTERNAL_DTD");
        check(fd.contains("SP_ACCESS_EXTERNAL_SCHEMA"), "field-SP_ACCESS_EXTERNAL_SCHEMA");
        check(fd.contains("ACCESS_EXTERNAL_ALL"), "field-ACCESS_EXTERNAL_ALL");
        check(fd.contains("EXTERNAL_ACCESS_DEFAULT_FSP"), "field-EXTERNAL_ACCESS_DEFAULT_FSP");
        check(fd.contains("EXTERNAL_ACCESS_DEFAULT"), "field-EXTERNAL_ACCESS_DEFAULT");
        check(fd.contains("XML_SECURITY_PROPERTY_MANAGER"), "field-XML_SECURITY_PROPERTY_MANAGER");
        check(fd.contains("CONFIG_FILE_PROPNAME"), "field-CONFIG_FILE_PROPNAME");
        check(fd.contains("DTD_PROPNAME"), "field-DTD_PROPNAME");
        check(fd.contains("ALLOW"), "field-ALLOW");
        check(fd.contains("IGNORE"), "field-IGNORE");
        check(fd.contains("DENY"), "field-DENY");
        check(fd.contains("JDKCATALOG_RESOLVE"), "field-JDKCATALOG_RESOLVE");
        check(fd.contains("CONTINUE"), "field-CONTINUE");
        check(fd.contains("STRICT"), "field-STRICT");
        check(fd.contains("FEATURE_TRUE"), "field-FEATURE_TRUE");
        check(fd.contains("FEATURE_FALSE"), "field-FEATURE_FALSE");
        check(fd.contains("S_IS_STANDALONE"), "field-S_IS_STANDALONE");
        check(fd.contains("FQ_IS_STANDALONE"), "field-FQ_IS_STANDALONE");
        check(fd.contains("SP_IS_STANDALONE"), "field-SP_IS_STANDALONE");
        check(fd.contains("ORACLE_IS_STANDALONE"), "field-ORACLE_IS_STANDALONE");
        check(fd.contains("JDK_IS_STANDALONE"), "field-JDK_IS_STANDALONE");
        check(fd.contains("SP_XSLTC_IS_STANDALONE"), "field-SP_XSLTC_IS_STANDALONE");
        check(fd.contains("ORACLE_ENABLE_EXTENSION_FUNCTION"), "field-ORACLE_ENABLE_EXTENSION_FUNCTION");
        check(fd.contains("SP_ENABLE_EXTENSION_FUNCTION"), "field-SP_ENABLE_EXTENSION_FUNCTION");
        check(fd.contains("SP_ENABLE_EXTENSION_FUNCTION_SPEC"), "field-SP_ENABLE_EXTENSION_FUNCTION_SPEC");
        check(fd.contains("RESET_SYMBOL_TABLE"), "field-RESET_SYMBOL_TABLE");
        check(fd.contains("RESET_SYMBOL_TABLE_DEFAULT"), "field-RESET_SYMBOL_TABLE_DEFAULT");
        check(fd.contains("OVERRIDE_PARSER"), "field-OVERRIDE_PARSER");
        check(fd.contains("OVERRIDE_PARSER_DEFAULT"), "field-OVERRIDE_PARSER_DEFAULT");
        check(fd.contains("CDATA_CHUNK_SIZE"), "field-CDATA_CHUNK_SIZE");
        check(fd.contains("CDATA_CHUNK_SIZE_DEFAULT"), "field-CDATA_CHUNK_SIZE_DEFAULT");
        check(fd.contains("SP_ENABLE_TEMPLATESIMPL_DESERIALIZATION"), "field-SP_ENABLE_TEMPLATESIMPL_DESERIALIZATION");
        check(md.size() == 0, "method-count");
        deep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static Object f(Class<?> c, String name) throws Exception {
        java.lang.reflect.Field fd = c.getDeclaredField(name);
        fd.setAccessible(true);
        return fd.get(null);
    }

    static void deep() throws Exception {
        // set System Properties BEFORE the first field read: JdkConstants captures
        // the *_DEFAULT values at class-initialization time (fresh JVM in jtreg)
        System.setProperty("jdk.xml.resetSymbolTable", "true");
        System.setProperty("jdk.xml.overrideDefaultParser", "true");
        System.setProperty("jdk.xml.cdataChunkSize", "18");
        Class<?> c = Class.forName("jdk.xml.internal.JdkConstants", true, JdkConstantsTest.class.getClassLoader());
        String P = "http://www.oracle.com/xml/jaxp/properties/";
        check(f(c, "SECURITY_MANAGER").equals("http://apache.org/xml/properties/security-manager"), "v-SECURITY_MANAGER");
        check(f(c, "ORACLE_JAXP_PROPERTY_PREFIX").equals(P), "v-ORACLE_JAXP_PROPERTY_PREFIX");
        check(f(c, "JDK_ENTITY_EXPANSION_LIMIT").equals(P + "entityExpansionLimit"), "v-JDK_ENTITY_EXPANSION_LIMIT");
        check(f(c, "JDK_ELEMENT_ATTRIBUTE_LIMIT").equals(P + "elementAttributeLimit"), "v-JDK_ELEMENT_ATTRIBUTE_LIMIT");
        check(f(c, "JDK_MAX_OCCUR_LIMIT").equals(P + "maxOccurLimit"), "v-JDK_MAX_OCCUR_LIMIT");
        check(f(c, "JDK_TOTAL_ENTITY_SIZE_LIMIT").equals(P + "totalEntitySizeLimit"), "v-JDK_TOTAL_ENTITY_SIZE_LIMIT");
        check(f(c, "JDK_GENERAL_ENTITY_SIZE_LIMIT").equals(P + "maxGeneralEntitySizeLimit"), "v-JDK_GENERAL_ENTITY_SIZE_LIMIT");
        check(f(c, "JDK_ENTITY_REPLACEMENT_LIMIT").equals(P + "entityReplacementLimit"), "v-JDK_ENTITY_REPLACEMENT_LIMIT");
        check(f(c, "JDK_PARAMETER_ENTITY_SIZE_LIMIT").equals(P + "maxParameterEntitySizeLimit"), "v-JDK_PARAMETER_ENTITY_SIZE_LIMIT");
        check(f(c, "JDK_XML_NAME_LIMIT").equals(P + "maxXMLNameLimit"), "v-JDK_XML_NAME_LIMIT");
        check(f(c, "JDK_MAX_ELEMENT_DEPTH").equals(P + "maxElementDepth"), "v-JDK_MAX_ELEMENT_DEPTH");
        check(f(c, "JDK_ENTITY_COUNT_INFO").equals(P + "getEntityCountInfo"), "v-JDK_ENTITY_COUNT_INFO");
        check(f(c, "JDK_DEBUG_LIMIT").equals("jdk.xml.getEntityCountInfo"), "v-JDK_DEBUG_LIMIT");
        check(f(c, "SP_ENTITY_EXPANSION_LIMIT").equals("jdk.xml.entityExpansionLimit"), "v-SP_ENTITY_EXPANSION_LIMIT");
        check(f(c, "SP_ELEMENT_ATTRIBUTE_LIMIT").equals("jdk.xml.elementAttributeLimit"), "v-SP_ELEMENT_ATTRIBUTE_LIMIT");
        check(f(c, "SP_MAX_OCCUR_LIMIT").equals("jdk.xml.maxOccurLimit"), "v-SP_MAX_OCCUR_LIMIT");
        check(f(c, "SP_TOTAL_ENTITY_SIZE_LIMIT").equals("jdk.xml.totalEntitySizeLimit"), "v-SP_TOTAL_ENTITY_SIZE_LIMIT");
        check(f(c, "SP_GENERAL_ENTITY_SIZE_LIMIT").equals("jdk.xml.maxGeneralEntitySizeLimit"), "v-SP_GENERAL_ENTITY_SIZE_LIMIT");
        check(f(c, "SP_ENTITY_REPLACEMENT_LIMIT").equals("jdk.xml.entityReplacementLimit"), "v-SP_ENTITY_REPLACEMENT_LIMIT");
        check(f(c, "SP_PARAMETER_ENTITY_SIZE_LIMIT").equals("jdk.xml.maxParameterEntitySizeLimit"), "v-SP_PARAMETER_ENTITY_SIZE_LIMIT");
        check(f(c, "SP_XML_NAME_LIMIT").equals("jdk.xml.maxXMLNameLimit"), "v-SP_XML_NAME_LIMIT");
        check(f(c, "SP_MAX_ELEMENT_DEPTH").equals("jdk.xml.maxElementDepth"), "v-SP_MAX_ELEMENT_DEPTH");
        check(f(c, "XPATH_GROUP_LIMIT").equals("jdk.xml.xpathExprGrpLimit"), "v-XPATH_GROUP_LIMIT");
        check(f(c, "XPATH_OP_LIMIT").equals("jdk.xml.xpathExprOpLimit"), "v-XPATH_OP_LIMIT");
        check(f(c, "XPATH_TOTALOP_LIMIT").equals("jdk.xml.xpathTotalOpLimit"), "v-XPATH_TOTALOP_LIMIT");
        check(f(c, "XPATH_ALLOW_NON_XSLT_SYS_PROPS").equals("jdk.xml.allowNonXSLTSysProps"), "v-XPATH_ALLOW_NON_XSLT_SYS_PROPS");
        check(f(c, "JDK_EXTENSION_CLASSLOADER").equals("jdk.xml.transform.extensionClassLoader"), "v-JDK_EXTENSION_CLASSLOADER");
        check(f(c, "JDK_EXT_CLASSLOADER").equals("jdk.xml.extensionClassLoader"), "v-JDK_EXT_CLASSLOADER");
        check(f(c, "ENTITY_EXPANSION_LIMIT").equals("entityExpansionLimit"), "v-ENTITY_EXPANSION_LIMIT");
        check(f(c, "ELEMENT_ATTRIBUTE_LIMIT").equals("elementAttributeLimit"), "v-ELEMENT_ATTRIBUTE_LIMIT");
        check(f(c, "MAX_OCCUR_LIMIT").equals("maxOccurLimit"), "v-MAX_OCCUR_LIMIT");
        check(f(c, "JDK_YES").equals("yes"), "v-JDK_YES");
        check(f(c, "ORACLE_FEATURE_SERVICE_MECHANISM").equals("http://www.oracle.com/feature/use-service-mechanism"), "v-ORACLE_FEATURE_SERVICE_MECHANISM");
        check(f(c, "SP_ACCESS_EXTERNAL_STYLESHEET").equals("javax.xml.accessExternalStylesheet"), "v-SP_ACCESS_EXTERNAL_STYLESHEET");
        check(f(c, "SP_ACCESS_EXTERNAL_DTD").equals("javax.xml.accessExternalDTD"), "v-SP_ACCESS_EXTERNAL_DTD");
        check(f(c, "SP_ACCESS_EXTERNAL_SCHEMA").equals("javax.xml.accessExternalSchema"), "v-SP_ACCESS_EXTERNAL_SCHEMA");
        check(f(c, "ACCESS_EXTERNAL_ALL").equals("all"), "v-ACCESS_EXTERNAL_ALL");
        check(f(c, "EXTERNAL_ACCESS_DEFAULT_FSP").equals(""), "v-EXTERNAL_ACCESS_DEFAULT_FSP");
        check(f(c, "EXTERNAL_ACCESS_DEFAULT").equals("all"), "v-EXTERNAL_ACCESS_DEFAULT");
        check(f(c, "XML_SECURITY_PROPERTY_MANAGER").equals("jdk.xml.xmlSecurityPropertyManager"), "v-XML_SECURITY_PROPERTY_MANAGER");
        check(f(c, "CONFIG_FILE_PROPNAME").equals("java.xml.config.file"), "v-CONFIG_FILE_PROPNAME");
        check(f(c, "DTD_PROPNAME").equals("jdk.xml.dtd.support"), "v-DTD_PROPNAME");
        check(f(c, "ALLOW").equals(Integer.valueOf(0)), "v-ALLOW");
        check(f(c, "IGNORE").equals(Integer.valueOf(1)), "v-IGNORE");
        check(f(c, "DENY").equals(Integer.valueOf(2)), "v-DENY");
        check(f(c, "JDKCATALOG_RESOLVE").equals("jdk.xml.jdkcatalog.resolve"), "v-JDKCATALOG_RESOLVE");
        check(f(c, "CONTINUE").equals(Integer.valueOf(0)), "v-CONTINUE");
        check(f(c, "STRICT").equals(Integer.valueOf(2)), "v-STRICT");
        check(f(c, "FEATURE_TRUE").equals("true"), "v-FEATURE_TRUE");
        check(f(c, "FEATURE_FALSE").equals("false"), "v-FEATURE_FALSE");
        check(f(c, "S_IS_STANDALONE").equals("isStandalone"), "v-S_IS_STANDALONE");
        check(f(c, "FQ_IS_STANDALONE").equals(P + "isStandalone"), "v-FQ_IS_STANDALONE");
        check(f(c, "SP_IS_STANDALONE").equals("jdk.xml.isStandalone"), "v-SP_IS_STANDALONE");
        check(f(c, "ORACLE_IS_STANDALONE").equals("http://www.oracle.com/xml/is-standalone"), "v-ORACLE_IS_STANDALONE");
        check(f(c, "JDK_IS_STANDALONE").equals(P + "xsltcIsStandalone"), "v-JDK_IS_STANDALONE");
        check(f(c, "SP_XSLTC_IS_STANDALONE").equals("jdk.xml.xsltcIsStandalone"), "v-SP_XSLTC_IS_STANDALONE");
        check(f(c, "ORACLE_ENABLE_EXTENSION_FUNCTION").equals(P + "enableExtensionFunctions"), "v-ORACLE_ENABLE_EXTENSION_FUNCTION");
        check(f(c, "SP_ENABLE_EXTENSION_FUNCTION").equals("javax.xml.enableExtensionFunctions"), "v-SP_ENABLE_EXTENSION_FUNCTION");
        check(f(c, "SP_ENABLE_EXTENSION_FUNCTION_SPEC").equals("jdk.xml.enableExtensionFunctions"), "v-SP_ENABLE_EXTENSION_FUNCTION_SPEC");
        check(f(c, "RESET_SYMBOL_TABLE").equals("jdk.xml.resetSymbolTable"), "v-RESET_SYMBOL_TABLE");
        check(f(c, "OVERRIDE_PARSER").equals("jdk.xml.overrideDefaultParser"), "v-OVERRIDE_PARSER");
        check(f(c, "CDATA_CHUNK_SIZE").equals("jdk.xml.cdataChunkSize"), "v-CDATA_CHUNK_SIZE");
        check(f(c, "SP_ENABLE_TEMPLATESIMPL_DESERIALIZATION").equals("jdk.xml.enableTemplatesImplDeserialization"), "v-SP_ENABLE_TEMPLATESIMPL_DESERIALIZATION");
        // defaults captured from System Properties at class init
        check(f(c, "RESET_SYMBOL_TABLE_DEFAULT").equals(Boolean.TRUE), "v-RESET_SYMBOL_TABLE_DEFAULT");
        check(f(c, "OVERRIDE_PARSER_DEFAULT").equals(Boolean.TRUE), "v-OVERRIDE_PARSER_DEFAULT");
        check(f(c, "CDATA_CHUNK_SIZE_DEFAULT").equals(Integer.valueOf(18)), "v-CDATA_CHUNK_SIZE_DEFAULT");
        System.clearProperty("jdk.xml.resetSymbolTable");
        System.clearProperty("jdk.xml.overrideDefaultParser");
        System.clearProperty("jdk.xml.cdataChunkSize");
        // class is final and holds only constants: no public ctor
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "class-final");
        try { c.getDeclaredConstructor(); check(true, "ctor-absent"); } catch (NoSuchMethodException e) { check(true, "ctor-absent-ok"); }
        // deprecated legacy names keep @Deprecated(since="17")
        String[] dep = {"ORACLE_JAXP_PROPERTY_PREFIX", "JDK_ENTITY_EXPANSION_LIMIT", "JDK_ELEMENT_ATTRIBUTE_LIMIT",
                "JDK_MAX_OCCUR_LIMIT", "JDK_TOTAL_ENTITY_SIZE_LIMIT", "JDK_GENERAL_ENTITY_SIZE_LIMIT",
                "JDK_ENTITY_REPLACEMENT_LIMIT", "JDK_PARAMETER_ENTITY_SIZE_LIMIT", "JDK_XML_NAME_LIMIT",
                "JDK_MAX_ELEMENT_DEPTH", "JDK_ENTITY_COUNT_INFO", "FQ_IS_STANDALONE", "ORACLE_IS_STANDALONE",
                "JDK_IS_STANDALONE", "ORACLE_FEATURE_SERVICE_MECHANISM"};
        for (String d : dep) {
            check(c.getDeclaredField(d).isAnnotationPresent(Deprecated.class), "deprecated-" + d);
        }
        // real-XML linkage: the constant names are live factory attributes
        javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf.setAttribute((String) f(c, "SP_ENTITY_EXPANSION_LIMIT"), "3");
        check(dbf.getAttribute((String) f(c, "SP_ENTITY_EXPANSION_LIMIT")).equals("3"), "factory-attr-sp-name");
        // legacy URI-style name has lower precedence: it cannot override an API-name set on the same factory
        dbf.setAttribute((String) f(c, "JDK_ENTITY_EXPANSION_LIMIT"), "4");
        check(dbf.getAttribute((String) f(c, "SP_ENTITY_EXPANSION_LIMIT")).equals("3"), "factory-attr-legacy-lower-precedence");
        javax.xml.parsers.DocumentBuilderFactory dbf2 = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        dbf2.setAttribute((String) f(c, "JDK_ENTITY_EXPANSION_LIMIT"), "4");
        check(dbf2.getAttribute((String) f(c, "SP_ENTITY_EXPANSION_LIMIT")).equals("4"), "factory-attr-legacy-name");
        dbf.setAttribute((String) f(c, "DTD_PROPNAME"), (String) f(c, "JDK_YES") == "yes" ? "deny" : "deny");
        check(dbf.getAttribute((String) f(c, "DTD_PROPNAME")).equals("deny"), "factory-attr-dtd");
        dbf.setAttribute((String) f(c, "JDK_DEBUG_LIMIT"), "yes");
        check(dbf.getAttribute((String) f(c, "JDK_DEBUG_LIMIT")).equals("yes"), "factory-attr-entitycount");
    }
}
