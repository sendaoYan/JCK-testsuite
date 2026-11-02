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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
