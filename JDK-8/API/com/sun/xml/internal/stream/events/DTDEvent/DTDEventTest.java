/*
 * @test
 * @summary com.sun.xml.internal.stream.events.DTDEvent conformance: class hierarchy and members.
 * @run main DTDEventTest
 */

public class DTDEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.events.DTDEvent", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.events.DTDEvent"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.events.DummyEvent".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.events.DummyEvent");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.events.DTD"), "has interface javax.xml.stream.events.DTD");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getDocumentTypeDeclaration"), "has method getDocumentTypeDeclaration");
        check(mn.contains("getEntities"), "has method getEntities");
        check(mn.contains("getNotations"), "has method getNotations");
        check(mn.contains("getProcessedDTD"), "has method getProcessedDTD");
        check(mn.contains("init"), "has method init");
        check(mn.contains("setDocumentTypeDeclaration"), "has method setDocumentTypeDeclaration");
        check(mn.contains("setEntities"), "has method setEntities");
        check(mn.contains("setNotations"), "has method setNotations");
        check(mn.contains("toString"), "has method toString");
        check(mn.contains("writeAsEncodedUnicodeEx"), "has method writeAsEncodedUnicodeEx");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
