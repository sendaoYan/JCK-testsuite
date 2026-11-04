/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serializer.ToSAXHandler conformance: abstract class hierarchy and members.
 * @run main ToSAXHandlerTest
 */
public class ToSAXHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serializer.ToSAXHandler", false, ToSAXHandlerTest.class.getClassLoader());
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.serializer.SerializerBase"), "superclass com.sun.org.apache.xml.internal.serializer.SerializerBase");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("startDocumentInternal"), "method startDocumentInternal");
        check(mn.contains("startDTD"), "method startDTD");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("closeStartTag"), "method closeStartTag");
        check(mn.contains("closeCDATA"), "method closeCDATA");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("setLexHandler"), "method setLexHandler");
        check(mn.contains("setContentHandler"), "method setContentHandler");
        check(mn.contains("setCdataSectionElements"), "method setCdataSectionElements");
        check(mn.contains("setShouldOutputNSAttr"), "method setShouldOutputNSAttr");
        check(mn.contains("getShouldOutputNSAttr"), "method getShouldOutputNSAttr");
        check(mn.contains("flushPending"), "method flushPending");
        check(mn.contains("setTransformState"), "method setTransformState");
        check(mn.contains("fatalError"), "method fatalError");
        check(mn.contains("error"), "method error");
        check(mn.contains("warning"), "method warning");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("addUniqueAttribute"), "method addUniqueAttribute");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
