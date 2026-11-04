/*
 * @test
 * @summary jdk.xml.internal.JdkXmlUtils conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main JdkXmlUtilsTest
 */
public class JdkXmlUtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkXmlUtils", false, JdkXmlUtilsTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.JdkXmlUtils"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
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
        check(fd.size() == 14, "field-count");
        check(fd.contains("IS_WINDOWS"), "field-IS_WINDOWS");
        check(fd.contains("JAVA_HOME"), "field-JAVA_HOME");
        check(fd.contains("NAMESPACES_FEATURE"), "field-NAMESPACES_FEATURE");
        check(fd.contains("NAMESPACE_PREFIXES_FEATURE"), "field-NAMESPACE_PREFIXES_FEATURE");
        check(fd.contains("USE_CATALOG"), "field-USE_CATALOG");
        check(fd.contains("SP_USE_CATALOG"), "field-SP_USE_CATALOG");
        check(fd.contains("CATALOG_FILES"), "field-CATALOG_FILES");
        check(fd.contains("CATALOG_DEFER"), "field-CATALOG_DEFER");
        check(fd.contains("CATALOG_PREFER"), "field-CATALOG_PREFER");
        check(fd.contains("CATALOG_RESOLVE"), "field-CATALOG_RESOLVE");
        check(fd.contains("RESOLVE_STRICT"), "field-RESOLVE_STRICT");
        check(fd.contains("RESOLVE_CONTINUE"), "field-RESOLVE_CONTINUE");
        check(fd.contains("RESOLVE_IGNORE"), "field-RESOLVE_IGNORE");
        check(fd.contains("USE_CATALOG_DEFAULT"), "field-USE_CATALOG_DEFAULT");
        check(md.size() == 17, "method-count");
        check(md.contains("catalogFeaturesConfig2Config"), "method-catalogFeaturesConfig2Config");
        check(md.contains("catalogFeaturesConfig2Reader"), "method-catalogFeaturesConfig2Reader");
        check(md.contains("getCatalogFeature"), "method-getCatalogFeature");
        check(md.contains("getCatalogFeatures"), "method-getCatalogFeatures");
        check(md.contains("getDOMDocument"), "method-getDOMDocument");
        check(md.contains("getDOMFactory"), "method-getDOMFactory");
        check(md.contains("getDTDExternalDecl"), "method-getDTDExternalDecl");
        check(md.contains("getProperty"), "method-getProperty");
        check(md.contains("getSAXFactory"), "method-getSAXFactory");
        check(md.contains("getSAXTransformFactory"), "method-getSAXTransformFactory");
        check(md.contains("getValue"), "method-getValue");
        check(md.contains("getXMLReader"), "method-getXMLReader");
        check(md.contains("initCatalogFeatures"), "method-initCatalogFeatures");
        check(md.contains("isResolveContinue"), "method-isResolveContinue");
        check(md.contains("setProperty"), "method-setProperty");
        check(md.contains("setReaderProperty"), "method-setReaderProperty");
        check(md.contains("setXMLReaderPropertyIfSupport"), "method-setXMLReaderPropertyIfSupport");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
