/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main XMLSecurityManagerTest
 */
public class XMLSecurityManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager", false, XMLSecurityManagerTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLSecurityManager"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
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
        check(fd.size() == 8, "field-count");
        check(fd.contains("DTD_KEY"), "field-DTD_KEY");
        check(fd.contains("DISALLOW_DTD"), "field-DISALLOW_DTD");
        check(fd.contains("LOAD_EXTERNAL_DTD"), "field-LOAD_EXTERNAL_DTD");
        check(fd.contains("ZEPHYR_PROPERTY_PREFIX"), "field-ZEPHYR_PROPERTY_PREFIX");
        check(fd.contains("IGNORE_EXTERNAL_DTD"), "field-IGNORE_EXTERNAL_DTD");
        check(fd.contains("DTD_ALLOW"), "field-DTD_ALLOW");
        check(fd.contains("DTD_IGNORE"), "field-DTD_IGNORE");
        check(fd.contains("DTD_DENY"), "field-DTD_DENY");
        check(md.size() == 25, "method-count");
        check(md.contains("clone"), "method-clone");
        check(md.contains("cloneAndUpdate"), "method-cloneAndUpdate");
        check(md.contains("convert"), "method-convert");
        check(md.contains("debugPrint"), "method-debugPrint");
        check(md.contains("find"), "method-find");
        check(md.contains("getEnumValue"), "method-getEnumValue");
        check(md.contains("getIndex"), "method-getIndex");
        check(md.contains("getJDKCatalogResolver"), "method-getJDKCatalogResolver");
        check(md.contains("getLimit"), "method-getLimit");
        check(md.contains("getLimitAsString"), "method-getLimitAsString");
        check(md.contains("getLimitValueAsString"), "method-getLimitValueAsString");
        check(md.contains("getLimitValueByIndex"), "method-getLimitValueByIndex");
        check(md.contains("getState"), "method-getState");
        check(md.contains("getStateLiteral"), "method-getStateLiteral");
        check(md.contains("is"), "method-is");
        check(md.contains("isNoLimit"), "method-isNoLimit");
        check(md.contains("isOverLimit"), "method-isOverLimit");
        check(md.contains("isSecureProcessing"), "method-isSecureProcessing");
        check(md.contains("isSecureProcessingSet"), "method-isSecureProcessingSet");
        check(md.contains("isSet"), "method-isSet");
        check(md.contains("printEntityCountInfo"), "method-printEntityCountInfo");
        check(md.contains("printWarning"), "method-printWarning");
        check(md.contains("readSystemProperties"), "method-readSystemProperties");
        check(md.contains("setLimit"), "method-setLimit");
        check(md.contains("setSecureProcessing"), "method-setSecureProcessing");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
