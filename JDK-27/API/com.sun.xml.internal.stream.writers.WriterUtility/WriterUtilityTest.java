/*
 * @test
 * @summary com.sun.xml.internal.stream.writers.WriterUtility conformance: internal package reflection.
 * @modules java.xml/com.sun.xml.internal.stream.writers:+open
 * @run main WriterUtilityTest
 */
public class WriterUtilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.writers.WriterUtility", false, WriterUtilityTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.writers.WriterUtility"), "name");
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
        check(fd.contains("START_COMMENT"), "field-START_COMMENT");
        check(fd.contains("END_COMMENT"), "field-END_COMMENT");
        check(fd.contains("DEFAULT_ENCODING"), "field-DEFAULT_ENCODING");
        check(fd.contains("DEFAULT_XMLDECL"), "field-DEFAULT_XMLDECL");
        check(fd.contains("DEFAULT_XML_VERSION"), "field-DEFAULT_XML_VERSION");
        check(fd.contains("CLOSE_START_TAG"), "field-CLOSE_START_TAG");
        check(fd.contains("OPEN_START_TAG"), "field-OPEN_START_TAG");
        check(fd.contains("OPEN_END_TAG"), "field-OPEN_END_TAG");
        check(fd.contains("CLOSE_END_TAG"), "field-CLOSE_END_TAG");
        check(fd.contains("START_CDATA"), "field-START_CDATA");
        check(fd.contains("END_CDATA"), "field-END_CDATA");
        check(fd.contains("CLOSE_EMPTY_ELEMENT"), "field-CLOSE_EMPTY_ELEMENT");
        check(fd.contains("SPACE"), "field-SPACE");
        check(fd.contains("UTF_8"), "field-UTF_8");
        check(md.size() == 5, "method-count");
        check(md.contains("getEscapeCharacters"), "method-getEscapeCharacters");
        check(md.contains("setEscapeCharacters"), "method-setEscapeCharacters");
        check(md.contains("setWriter"), "method-setWriter");
        check(md.contains("writeXMLAttributeValue"), "method-writeXMLAttributeValue");
        check(md.contains("writeXMLContent"), "method-writeXMLContent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
