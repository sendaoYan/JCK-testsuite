/*
 * @test
 * @summary com.sun.xml.internal.stream.events.StartDocumentEvent conformance: class hierarchy and members.
 * @run main StartDocumentEventTest
 */

public class StartDocumentEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.stream.events.StartDocumentEvent", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("com.sun.xml.internal.stream.events.StartDocumentEvent"), "binary name");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "is public");
        check(!c.isInterface(), "is not interface");
        check(!c.isEnum(), "is not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete");
        check("com.sun.xml.internal.stream.events.DummyEvent".equals(c.getSuperclass().getName()), "extends com.sun.xml.internal.stream.events.DummyEvent");
        java.util.Set<String> ifs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "direct interface count");
        check(ifs.contains("javax.xml.stream.events.StartDocument"), "has interface javax.xml.stream.events.StartDocument");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("clear"), "has method clear");
        check(mn.contains("encodingSet"), "has method encodingSet");
        check(mn.contains("getCharacterEncodingScheme"), "has method getCharacterEncodingScheme");
        check(mn.contains("getSystemId"), "has method getSystemId");
        check(mn.contains("getVersion"), "has method getVersion");
        check(mn.contains("init"), "has method init");
        check(mn.contains("isStandalone"), "has method isStandalone");
        check(mn.contains("isStartDocument"), "has method isStartDocument");
        check(mn.contains("setDeclaredEncoding"), "has method setDeclaredEncoding");
        check(mn.contains("setEncoding"), "has method setEncoding");
        check(mn.contains("setStandalone"), "has method setStandalone");
        check(mn.contains("setVersion"), "has method setVersion");
        check(mn.contains("standaloneSet"), "has method standaloneSet");
        check(mn.contains("toString"), "has method toString");
        check(mn.contains("writeAsEncodedUnicodeEx"), "has method writeAsEncodedUnicodeEx");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
