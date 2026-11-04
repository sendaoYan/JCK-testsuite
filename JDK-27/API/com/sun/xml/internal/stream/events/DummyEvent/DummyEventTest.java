/*
 * @test
 * @summary com.sun.xml.internal.stream.events.DummyEvent conformance: internal package reflection.
 * @modules java.xml/com.sun.xml.internal.stream.events:+open
 * @run main DummyEventTest
 */
public class DummyEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.events.DummyEvent", false, DummyEventTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.events.DummyEvent"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("javax.xml.stream.events.XMLEvent"), "if-javax.xml.stream.events.XMLEvent");
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
        check(fd.size() == 1, "field-count");
        check(fd.contains("fLocation"), "field-fLocation");
        check(md.size() == 20, "method-count");
        check(md.contains("asCharacters"), "method-asCharacters");
        check(md.contains("asEndElement"), "method-asEndElement");
        check(md.contains("asStartElement"), "method-asStartElement");
        check(md.contains("charEncode"), "method-charEncode");
        check(md.contains("getEventType"), "method-getEventType");
        check(md.contains("getLocation"), "method-getLocation");
        check(md.contains("getSchemaType"), "method-getSchemaType");
        check(md.contains("isAttribute"), "method-isAttribute");
        check(md.contains("isCharacterData"), "method-isCharacterData");
        check(md.contains("isCharacters"), "method-isCharacters");
        check(md.contains("isEndDocument"), "method-isEndDocument");
        check(md.contains("isEndElement"), "method-isEndElement");
        check(md.contains("isEntityReference"), "method-isEntityReference");
        check(md.contains("isNamespace"), "method-isNamespace");
        check(md.contains("isProcessingInstruction"), "method-isProcessingInstruction");
        check(md.contains("isStartDocument"), "method-isStartDocument");
        check(md.contains("isStartElement"), "method-isStartElement");
        check(md.contains("setEventType"), "method-setEventType");
        check(md.contains("writeAsEncodedUnicode"), "method-writeAsEncodedUnicode");
        check(md.contains("writeAsEncodedUnicodeEx"), "method-writeAsEncodedUnicodeEx");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
