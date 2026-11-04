/*
 * @test
 * @summary jdk.xml.internal.JdkXmlFeatures conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main JdkXmlFeaturesTest
 */
public class JdkXmlFeaturesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.JdkXmlFeatures", false, JdkXmlFeaturesTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.JdkXmlFeatures"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("java.lang.Cloneable"), "if-java.lang.Cloneable");
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
        check(fd.size() == 4, "field-count");
        check(fd.contains("ORACLE_JAXP_PROPERTY_PREFIX"), "field-ORACLE_JAXP_PROPERTY_PREFIX");
        check(fd.contains("XML_FEATURE_MANAGER"), "field-XML_FEATURE_MANAGER");
        check(fd.contains("CATALOG_FEATURES"), "field-CATALOG_FEATURES");
        check(fd.contains("PROPERTY_USE_CATALOG"), "field-PROPERTY_USE_CATALOG");
        check(md.size() == 7, "method-count");
        check(md.contains("clone"), "method-clone");
        check(md.contains("cloneAndUpdate"), "method-cloneAndUpdate");
        check(md.contains("findByName"), "method-findByName");
        check(md.contains("getFeature"), "method-getFeature");
        check(md.contains("getIndex"), "method-getIndex");
        check(md.contains("setFeature"), "method-setFeature");
        check(md.contains("update"), "method-update");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
