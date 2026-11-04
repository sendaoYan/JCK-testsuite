/*
 * @test
 * @summary com.sun.xml.internal.stream.events.DummyEvent conformance: abstract class hierarchy and members.
 * @run main DummyEventTest
 */

public class DummyEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.events.DummyEvent", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.events.DummyEvent"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "extends java.lang.Object");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.events.XMLEvent"), "has interface javax.xml.stream.events.XMLEvent");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("asCharacters"), "has method asCharacters");
        check(mn.contains("asEndElement"), "has method asEndElement");
        check(mn.contains("asStartElement"), "has method asStartElement");
        check(mn.contains("charEncode"), "has method charEncode");
        check(mn.contains("getEventType"), "has method getEventType");
        check(mn.contains("getLocation"), "has method getLocation");
        check(mn.contains("getSchemaType"), "has method getSchemaType");
        check(mn.contains("isAttribute"), "has method isAttribute");
        check(mn.contains("isCharacterData"), "has method isCharacterData");
        check(mn.contains("isCharacters"), "has method isCharacters");
        check(mn.contains("isEndDocument"), "has method isEndDocument");
        check(mn.contains("isEndElement"), "has method isEndElement");
        check(mn.contains("isEntityReference"), "has method isEntityReference");
        check(mn.contains("isNamespace"), "has method isNamespace");
        check(mn.contains("isProcessingInstruction"), "has method isProcessingInstruction");
        check(mn.contains("isStartDocument"), "has method isStartDocument");
        check(mn.contains("isStartElement"), "has method isStartElement");
        check(mn.contains("setEventType"), "has method setEventType");
        check(mn.contains("setLocation"), "has method setLocation");
        check(mn.contains("writeAsEncodedUnicode"), "has method writeAsEncodedUnicode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
