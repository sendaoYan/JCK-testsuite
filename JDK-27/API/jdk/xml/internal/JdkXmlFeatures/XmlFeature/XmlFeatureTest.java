/*
 * @test
 * @summary jdk.xml.internal.JdkXmlFeatures.XmlFeature conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main XmlFeatureTest
 */
public class XmlFeatureTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkXmlFeatures$XmlFeature", false, XmlFeatureTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.JdkXmlFeatures$XmlFeature"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
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
        check(ec.size() == 6, "enum-constant-count");
        check(ec.contains("ENABLE_EXTENSION_FUNCTION"), "enum-ENABLE_EXTENSION_FUNCTION");
        check(ec.contains("USE_CATALOG"), "enum-USE_CATALOG");
        check(ec.contains("RESET_SYMBOL_TABLE_FEATURE"), "enum-RESET_SYMBOL_TABLE_FEATURE");
        check(ec.contains("JDK_OVERRIDE_PARSER"), "enum-JDK_OVERRIDE_PARSER");
        check(ec.contains("JDK_XPATH_SYSTEM_PROPERTY"), "enum-JDK_XPATH_SYSTEM_PROPERTY");
        check(ec.contains("ENABLE_TEMPLATESIMPL_DESERIALIZATION"), "enum-ENABLE_TEMPLATESIMPL_DESERIALIZATION");
        check(fd.size() == 0, "field-count");
        check(md.size() == 6, "method-count");
        check(md.contains("apiProperty"), "method-apiProperty");
        check(md.contains("defaultValue"), "method-defaultValue");
        check(md.contains("enforcedValue"), "method-enforcedValue");
        check(md.contains("getState"), "method-getState");
        check(md.contains("valueOf"), "method-valueOf");
        check(md.contains("values"), "method-values");
        deep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static Object e(Class<?> c, String n) { for (Object o : c.getEnumConstants()) if (o.toString().equals(n)) return o; throw new AssertionError("no " + n); }
    static Object inv(Object t, String n, Object... a) throws Exception {
        for (Class<?> k = t instanceof Class ? (Class<?>) t : t.getClass(); k != null; k = k.getSuperclass()) {
            for (java.lang.reflect.Method m : k.getDeclaredMethods()) {
                if (m.getName().equals(n) && m.getParameterCount() == a.length) { m.setAccessible(true); return m.invoke(t instanceof Class ? null : t, a); }
            }
        }
        throw new NoSuchMethodException(n);
    }

    static void deep() throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkXmlFeatures$XmlFeature", false, XmlFeatureTest.class.getClassLoader());
        Object[] vals = c.getEnumConstants();
        String[] order = {"ENABLE_EXTENSION_FUNCTION", "USE_CATALOG", "RESET_SYMBOL_TABLE_FEATURE", "JDK_OVERRIDE_PARSER", "JDK_XPATH_SYSTEM_PROPERTY", "ENABLE_TEMPLATESIMPL_DESERIALIZATION"};
        check(vals.length == 6, "values-length-6");
        for (int i = 0; i < order.length; i++) {
            check(vals[i].toString().equals(order[i]), "order-" + order[i]);
            check(((Enum<?>) vals[i]).ordinal() == i, "ordinal-" + order[i]);
        }
        String P = "http://www.oracle.com/xml/jaxp/properties/";
        // apiProperty(): the factory-visible attribute name
        check(inv(e(c, "ENABLE_EXTENSION_FUNCTION"), "apiProperty").equals(P + "enableExtensionFunctions"), "api-EEF");
        check(inv(e(c, "USE_CATALOG"), "apiProperty").equals(javax.xml.XMLConstants.USE_CATALOG), "api-UC");
        check(inv(e(c, "RESET_SYMBOL_TABLE_FEATURE"), "apiProperty").equals("jdk.xml.resetSymbolTable"), "api-RST");
        check(inv(e(c, "JDK_OVERRIDE_PARSER"), "apiProperty").equals("jdk.xml.overrideDefaultParser"), "api-ODP");
        check(inv(e(c, "JDK_XPATH_SYSTEM_PROPERTY"), "apiProperty").equals("jdk.xml.allowNonXSLTSysProps"), "api-XSP");
        check(inv(e(c, "ENABLE_TEMPLATESIMPL_DESERIALIZATION"), "apiProperty").equals("jdk.xml.enableTemplatesImplDeserialization"), "api-TPL");
        // package-private systemProperty()/systemPropertyOld()/hasSystemProperty()/enforced()
        check(inv(e(c, "ENABLE_EXTENSION_FUNCTION"), "systemProperty").equals("jdk.xml.enableExtensionFunctions"), "sp-EEF");
        check(inv(e(c, "ENABLE_EXTENSION_FUNCTION"), "systemPropertyOld").equals("javax.xml.enableExtensionFunctions"), "spOld-EEF");
        check(inv(e(c, "USE_CATALOG"), "systemProperty").equals("javax.xml.useCatalog"), "sp-UC");
        check(inv(e(c, "USE_CATALOG"), "systemPropertyOld") == null, "spOld-UC-null");
        check(inv(e(c, "RESET_SYMBOL_TABLE_FEATURE"), "systemProperty").equals("jdk.xml.resetSymbolTable"), "sp-RST");
        check(inv(e(c, "JDK_OVERRIDE_PARSER"), "systemProperty").equals("jdk.xml.overrideDefaultParser"), "sp-ODP");
        check(inv(e(c, "JDK_OVERRIDE_PARSER"), "systemPropertyOld").equals("http://www.oracle.com/feature/use-service-mechanism"), "spOld-ODP");
        check(inv(e(c, "JDK_XPATH_SYSTEM_PROPERTY"), "systemProperty").equals("jdk.xml.allowNonXSLTSysProps"), "sp-XSP");
        check(inv(e(c, "ENABLE_TEMPLATESIMPL_DESERIALIZATION"), "systemProperty").equals("jdk.xml.enableTemplatesImplDeserialization"), "sp-TPL");
        // default and FSP-enforced values
        check(inv(e(c, "ENABLE_EXTENSION_FUNCTION"), "defaultValue").equals(Boolean.FALSE), "def-EEF");
        check(inv(e(c, "USE_CATALOG"), "defaultValue").equals(Boolean.TRUE), "def-UC");
        check(inv(e(c, "RESET_SYMBOL_TABLE_FEATURE"), "defaultValue").equals(Boolean.FALSE), "def-RST");
        check(inv(e(c, "JDK_OVERRIDE_PARSER"), "defaultValue").equals(Boolean.FALSE), "def-ODP");
        check(inv(e(c, "JDK_XPATH_SYSTEM_PROPERTY"), "defaultValue").equals(Boolean.FALSE), "def-XSP");
        check(inv(e(c, "ENABLE_TEMPLATESIMPL_DESERIALIZATION"), "defaultValue").equals(Boolean.FALSE), "def-TPL");
        for (String n : order) check(inv(e(c, n), "enforcedValue").equals(Boolean.FALSE), "enfVal-" + n);
        check((Boolean) inv(e(c, "ENABLE_EXTENSION_FUNCTION"), "enforced"), "enforced-EEF-true");
        check((Boolean) inv(e(c, "JDK_XPATH_SYSTEM_PROPERTY"), "enforced"), "enforced-XSP-true");
        check((Boolean) inv(e(c, "ENABLE_TEMPLATESIMPL_DESERIALIZATION"), "enforced"), "enforced-TPL-true");
        check(!(Boolean) inv(e(c, "USE_CATALOG"), "enforced"), "enforced-UC-false");
        check(!(Boolean) inv(e(c, "RESET_SYMBOL_TABLE_FEATURE"), "enforced"), "enforced-RST-false");
        check(!(Boolean) inv(e(c, "JDK_OVERRIDE_PARSER"), "enforced"), "enforced-ODP-false");
        for (String n : order) check((Boolean) inv(e(c, n), "hasSystemProperty"), "hasSys-" + n);
        // package-private equalsPropertyName
        check((Boolean) inv(e(c, "ENABLE_EXTENSION_FUNCTION"), "equalsPropertyName", "jdk.xml.enableExtensionFunctions"), "eqName-EEF-sp");
        check((Boolean) inv(e(c, "ENABLE_EXTENSION_FUNCTION"), "equalsPropertyName", P + "enableExtensionFunctions"), "eqName-EEF-q");
        check(!(Boolean) inv(e(c, "ENABLE_EXTENSION_FUNCTION"), "equalsPropertyName", "javax.xml.enableExtensionFunctions"), "eqName-EEF-spOld-false");
        check((Boolean) inv(e(c, "USE_CATALOG"), "equalsPropertyName", javax.xml.XMLConstants.USE_CATALOG), "eqName-UC-api");
        check(!(Boolean) inv(e(c, "USE_CATALOG"), "equalsPropertyName", "javax.xml.useCatalog"), "eqName-UC-sp-false");
        check((Boolean) inv(e(c, "JDK_OVERRIDE_PARSER"), "equalsPropertyName", "http://www.oracle.com/feature/use-service-mechanism"), "eqName-ODP-legacy");
        check(!(Boolean) inv(e(c, "JDK_XPATH_SYSTEM_PROPERTY"), "equalsPropertyName", "bogus"), "eqName-XSP-bogus");
        // getState(String): name classification
        check(inv(e(c, "ENABLE_EXTENSION_FUNCTION"), "getState", "jdk.xml.enableExtensionFunctions").toString().equals("APIPROPERTY"), "getState-EEF-api");
        check(inv(e(c, "ENABLE_EXTENSION_FUNCTION"), "getState", P + "enableExtensionFunctions").toString().equals("LEGACY_APIPROPERTY"), "getState-EEF-legacy");
        check(inv(e(c, "ENABLE_EXTENSION_FUNCTION"), "getState", "javax.xml.enableExtensionFunctions") == null, "getState-EEF-spOld-null");
        check(inv(e(c, "USE_CATALOG"), "getState", javax.xml.XMLConstants.USE_CATALOG).toString().equals("APIPROPERTY"), "getState-UC-api");
        check(inv(e(c, "USE_CATALOG"), "getState", "javax.xml.useCatalog") == null, "getState-UC-sp-null");
        check(inv(e(c, "JDK_OVERRIDE_PARSER"), "getState", "jdk.xml.overrideDefaultParser").toString().equals("APIPROPERTY"), "getState-ODP-api");
        check(inv(e(c, "JDK_XPATH_SYSTEM_PROPERTY"), "getState", "jdk.xml.allowNonXSLTSysProps").toString().equals("APIPROPERTY"), "getState-XSP-api");
        check(inv(e(c, "JDK_OVERRIDE_PARSER"), "getState", "bogus") == null, "getState-bogus-null");
        // valueOf round trip for all six constants
        for (Object v : vals) check(java.lang.Enum.valueOf((Class<Enum>) (Class) c, v.toString()) == v, "valueOf-" + v);
        // real-XML linkage: two apiProperty() names are live factory attributes/features
        javax.xml.parsers.SAXParserFactory spf = javax.xml.parsers.SAXParserFactory.newInstance();
        spf.setFeature("jdk.xml.resetSymbolTable", true);
        check(spf.getFeature("jdk.xml.resetSymbolTable"), "sax-reset-symbol-table");
        javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
        tf.setFeature(javax.xml.XMLConstants.USE_CATALOG, false);
        check(!tf.getFeature(javax.xml.XMLConstants.USE_CATALOG), "tf-use-catalog-feature");
    }
}
