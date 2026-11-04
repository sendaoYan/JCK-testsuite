/*
 * @test
 * @summary com.sun.xml.internal.stream.XMLEventReaderImpl conformance: internal package reflection.
 * @modules java.xml/com.sun.xml.internal.stream:+open
 * @run main XMLEventReaderImplTest
 */
public class XMLEventReaderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.XMLEventReaderImpl", false, XMLEventReaderImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.XMLEventReaderImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("javax.xml.stream.XMLEventReader"), "if-javax.xml.stream.XMLEventReader");
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
        check(fd.size() == 2, "field-count");
        check(fd.contains("fXMLReader"), "field-fXMLReader");
        check(fd.contains("fXMLEventAllocator"), "field-fXMLEventAllocator");
        check(md.size() == 9, "method-count");
        check(md.contains("close"), "method-close");
        check(md.contains("getElementText"), "method-getElementText");
        check(md.contains("getProperty"), "method-getProperty");
        check(md.contains("hasNext"), "method-hasNext");
        check(md.contains("next"), "method-next");
        check(md.contains("nextEvent"), "method-nextEvent");
        check(md.contains("nextTag"), "method-nextTag");
        check(md.contains("peek"), "method-peek");
        check(md.contains("remove"), "method-remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
