/*
 * @test
 * @summary jdk.xml.internal.XMLSecurityManager.Limit conformance: internal package reflection.
 * @modules java.xml/jdk.xml.internal:+open
 * @run main LimitTest
 */
public class LimitTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.xml.internal.XMLSecurityManager$Limit", false, LimitTest.class.getClassLoader());
        check(c.getName().equals("jdk.xml.internal.XMLSecurityManager$Limit"), "name");
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
        check(ec.size() == 16, "enum-constant-count");
        check(ec.contains("ENTITY_EXPANSION_LIMIT"), "enum-ENTITY_EXPANSION_LIMIT");
        check(ec.contains("MAX_OCCUR_NODE_LIMIT"), "enum-MAX_OCCUR_NODE_LIMIT");
        check(ec.contains("ELEMENT_ATTRIBUTE_LIMIT"), "enum-ELEMENT_ATTRIBUTE_LIMIT");
        check(ec.contains("TOTAL_ENTITY_SIZE_LIMIT"), "enum-TOTAL_ENTITY_SIZE_LIMIT");
        check(ec.contains("GENERAL_ENTITY_SIZE_LIMIT"), "enum-GENERAL_ENTITY_SIZE_LIMIT");
        check(ec.contains("PARAMETER_ENTITY_SIZE_LIMIT"), "enum-PARAMETER_ENTITY_SIZE_LIMIT");
        check(ec.contains("MAX_ELEMENT_DEPTH_LIMIT"), "enum-MAX_ELEMENT_DEPTH_LIMIT");
        check(ec.contains("MAX_NAME_LIMIT"), "enum-MAX_NAME_LIMIT");
        check(ec.contains("ENTITY_REPLACEMENT_LIMIT"), "enum-ENTITY_REPLACEMENT_LIMIT");
        check(ec.contains("XPATH_GROUP_LIMIT"), "enum-XPATH_GROUP_LIMIT");
        check(ec.contains("XPATH_OP_LIMIT"), "enum-XPATH_OP_LIMIT");
        check(ec.contains("XPATH_TOTALOP_LIMIT"), "enum-XPATH_TOTALOP_LIMIT");
        check(ec.contains("DTD"), "enum-DTD");
        check(ec.contains("XERCES_DISALLOW_DTD"), "enum-XERCES_DISALLOW_DTD");
        check(ec.contains("STAX_SUPPORT_DTD"), "enum-STAX_SUPPORT_DTD");
        check(ec.contains("JDKCATALOG_RESOLVE"), "enum-JDKCATALOG_RESOLVE");
        check(fd.size() == 0, "field-count");
        check(md.size() == 11, "method-count");
        check(md.contains("apiProperty"), "method-apiProperty");
        check(md.contains("defaultValue"), "method-defaultValue");
        check(md.contains("getState"), "method-getState");
        check(md.contains("is"), "method-is");
        check(md.contains("isSupported"), "method-isSupported");
        check(md.contains("key"), "method-key");
        check(md.contains("mapper"), "method-mapper");
        check(md.contains("spOld"), "method-spOld");
        check(md.contains("systemProperty"), "method-systemProperty");
        check(md.contains("valueOf"), "method-valueOf");
        check(md.contains("values"), "method-values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
