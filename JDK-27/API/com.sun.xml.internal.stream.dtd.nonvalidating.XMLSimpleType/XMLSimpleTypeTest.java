/*
 * @test
 * @summary com.sun.xml.internal.stream.dtd.nonvalidating.XMLSimpleType conformance: internal package reflection.
 * @modules java.xml/com.sun.xml.internal.stream.dtd.nonvalidating:+open
 * @run main XMLSimpleTypeTest
 */
public class XMLSimpleTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.dtd.nonvalidating.XMLSimpleType", false, XMLSimpleTypeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.dtd.nonvalidating.XMLSimpleType"), "name");
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
        check(fd.size() == 19, "field-count");
        check(fd.contains("TYPE_CDATA"), "field-TYPE_CDATA");
        check(fd.contains("TYPE_ENTITY"), "field-TYPE_ENTITY");
        check(fd.contains("TYPE_ENUMERATION"), "field-TYPE_ENUMERATION");
        check(fd.contains("TYPE_ID"), "field-TYPE_ID");
        check(fd.contains("TYPE_IDREF"), "field-TYPE_IDREF");
        check(fd.contains("TYPE_NMTOKEN"), "field-TYPE_NMTOKEN");
        check(fd.contains("TYPE_NOTATION"), "field-TYPE_NOTATION");
        check(fd.contains("TYPE_NAMED"), "field-TYPE_NAMED");
        check(fd.contains("DEFAULT_TYPE_DEFAULT"), "field-DEFAULT_TYPE_DEFAULT");
        check(fd.contains("DEFAULT_TYPE_FIXED"), "field-DEFAULT_TYPE_FIXED");
        check(fd.contains("DEFAULT_TYPE_IMPLIED"), "field-DEFAULT_TYPE_IMPLIED");
        check(fd.contains("DEFAULT_TYPE_REQUIRED"), "field-DEFAULT_TYPE_REQUIRED");
        check(fd.contains("type"), "field-type");
        check(fd.contains("name"), "field-name");
        check(fd.contains("enumeration"), "field-enumeration");
        check(fd.contains("list"), "field-list");
        check(fd.contains("defaultType"), "field-defaultType");
        check(fd.contains("defaultValue"), "field-defaultValue");
        check(fd.contains("nonNormalizedDefaultValue"), "field-nonNormalizedDefaultValue");
        check(md.size() == 2, "method-count");
        check(md.contains("clear"), "method-clear");
        check(md.contains("setValues"), "method-setValues");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
