/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.ToTextStream conformance: class hierarchy and members.
 * @run main ToTextStreamTest
 */
public class ToTextStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.ToTextStream", false, ToTextStreamTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.serializer.ToStream"), "superclass com.sun.org.apache.xml.internal.serializer.ToStream");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("startDocumentInternal"), "method startDocumentInternal");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("charactersRaw"), "method charactersRaw");
        check(mn.contains("writeNormalizedChars"), "method writeNormalizedChars");
        check(mn.contains("cdata"), "method cdata");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("entityReference"), "method entityReference");
        check(mn.contains("addAttribute"), "method addAttribute");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("addUniqueAttribute"), "method addUniqueAttribute");
        check(mn.contains("startPrefixMapping"), "method startPrefixMapping");
        check(mn.contains("namespaceAfterStartElement"), "method namespaceAfterStartElement");
        check(mn.contains("flushPending"), "method flushPending");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
