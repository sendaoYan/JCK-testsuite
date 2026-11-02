/*
 * @test
 * @summary com.sun.org.apache.xml.internal.serialize.HTMLSerializer conformance: class hierarchy and members.
 * @run main HTMLSerializerTest
 */
public class HTMLSerializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.serialize.HTMLSerializer", false, HTMLSerializerTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer"), "superclass com.sun.org.apache.xml.internal.serialize.BaseMarkupSerializer");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("setOutputFormat"), "method setOutputFormat");
        check(mn.contains("setXHTMLNamespace"), "method setXHTMLNamespace");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("endElementIO"), "method endElementIO");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("serializeElement"), "method serializeElement");
        check(mn.contains("getEntityRef"), "method getEntityRef");
        check(mn.contains("escapeURI"), "method escapeURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
