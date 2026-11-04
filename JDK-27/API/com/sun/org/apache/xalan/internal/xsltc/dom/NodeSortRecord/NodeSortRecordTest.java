/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.NodeSortRecord conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.dom:+open
 * @run main NodeSortRecordTest
 */
public class NodeSortRecordTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.NodeSortRecord", false, NodeSortRecordTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.dom.NodeSortRecord"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
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
        check(fd.size() == 8, "field-count");
        check(fd.contains("COMPARE_STRING"), "field-COMPARE_STRING");
        check(fd.contains("COMPARE_NUMERIC"), "field-COMPARE_NUMERIC");
        check(fd.contains("COMPARE_ASCENDING"), "field-COMPARE_ASCENDING");
        check(fd.contains("COMPARE_DESCENDING"), "field-COMPARE_DESCENDING");
        check(fd.contains("_collator"), "field-_collator");
        check(fd.contains("_collators"), "field-_collators");
        check(fd.contains("_locale"), "field-_locale");
        check(fd.contains("_settings"), "field-_settings");
        check(md.size() == 6, "method-count");
        check(md.contains("compareDocOrder"), "method-compareDocOrder");
        check(md.contains("compareTo"), "method-compareTo");
        check(md.contains("extractValueFromDOM"), "method-extractValueFromDOM");
        check(md.contains("getCollator"), "method-getCollator");
        check(md.contains("getNode"), "method-getNode");
        check(md.contains("initialize"), "method-initialize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
