/*
 * @test
 * @summary com.sun.xml.internal.stream.dtd.nonvalidating.XMLElementDecl conformance: internal package reflection.
 * @modules java.xml/com.sun.xml.internal.stream.dtd.nonvalidating:+open
 * @run main XMLElementDeclTest
 */
public class XMLElementDeclTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.dtd.nonvalidating.XMLElementDecl", false, XMLElementDeclTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.dtd.nonvalidating.XMLElementDecl"), "name");
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
        check(fd.size() == 9, "field-count");
        check(fd.contains("TYPE_ANY"), "field-TYPE_ANY");
        check(fd.contains("TYPE_EMPTY"), "field-TYPE_EMPTY");
        check(fd.contains("TYPE_MIXED"), "field-TYPE_MIXED");
        check(fd.contains("TYPE_CHILDREN"), "field-TYPE_CHILDREN");
        check(fd.contains("TYPE_SIMPLE"), "field-TYPE_SIMPLE");
        check(fd.contains("name"), "field-name");
        check(fd.contains("scope"), "field-scope");
        check(fd.contains("type"), "field-type");
        check(fd.contains("simpleType"), "field-simpleType");
        check(md.size() == 2, "method-count");
        check(md.contains("clear"), "method-clear");
        check(md.contains("setValues"), "method-setValues");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
