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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
