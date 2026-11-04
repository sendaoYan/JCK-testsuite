/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.dom:+open
 * @run main NodeCounterTest
 */
public class NodeCounterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter", false, NodeCounterTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.dom.NodeCounter"), "name");
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
        check(fd.size() == 13, "field-count");
        check(fd.contains("END"), "field-END");
        check(fd.contains("_node"), "field-_node");
        check(fd.contains("_nodeType"), "field-_nodeType");
        check(fd.contains("_value"), "field-_value");
        check(fd.contains("_document"), "field-_document");
        check(fd.contains("_iterator"), "field-_iterator");
        check(fd.contains("_translet"), "field-_translet");
        check(fd.contains("_format"), "field-_format");
        check(fd.contains("_lang"), "field-_lang");
        check(fd.contains("_letterValue"), "field-_letterValue");
        check(fd.contains("_groupSep"), "field-_groupSep");
        check(fd.contains("_groupSize"), "field-_groupSize");
        check(fd.contains("_hasFrom"), "field-_hasFrom");
        check(md.size() == 8, "method-count");
        check(md.contains("formatNumbers"), "method-formatNumbers");
        check(md.contains("getCounter"), "method-getCounter");
        check(md.contains("matchesCount"), "method-matchesCount");
        check(md.contains("matchesFrom"), "method-matchesFrom");
        check(md.contains("setDefaultFormatting"), "method-setDefaultFormatting");
        check(md.contains("setFormatting"), "method-setFormatting");
        check(md.contains("setStartNode"), "method-setStartNode");
        check(md.contains("setValue"), "method-setValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
