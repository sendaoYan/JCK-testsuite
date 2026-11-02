/*
 * @test
 * @summary com.sun.xml.internal.stream.events.XMLEventFactoryImpl conformance: internal package reflection.
 * @modules java.xml/com.sun.xml.internal.stream.events:+open
 * @run main XMLEventFactoryImplTest
 */
public class XMLEventFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.events.XMLEventFactoryImpl", false, XMLEventFactoryImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.events.XMLEventFactoryImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.xml.stream.XMLEventFactory"), "superclass");
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
        check(fd.size() == 0, "field-count");
        check(md.size() == 15, "method-count");
        check(md.contains("createAttribute"), "method-createAttribute");
        check(md.contains("createCData"), "method-createCData");
        check(md.contains("createCharacters"), "method-createCharacters");
        check(md.contains("createComment"), "method-createComment");
        check(md.contains("createDTD"), "method-createDTD");
        check(md.contains("createEndDocument"), "method-createEndDocument");
        check(md.contains("createEndElement"), "method-createEndElement");
        check(md.contains("createEntityReference"), "method-createEntityReference");
        check(md.contains("createIgnorableSpace"), "method-createIgnorableSpace");
        check(md.contains("createNamespace"), "method-createNamespace");
        check(md.contains("createProcessingInstruction"), "method-createProcessingInstruction");
        check(md.contains("createSpace"), "method-createSpace");
        check(md.contains("createStartDocument"), "method-createStartDocument");
        check(md.contains("createStartElement"), "method-createStartElement");
        check(md.contains("setLocation"), "method-setLocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
