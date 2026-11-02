/*
 * @test
 * @summary com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaDOMParser conformance: class hierarchy and members.
 * @run main SchemaDOMParserTest
 */
public class SchemaDOMParserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xerces.internal.impl.xs.opti.SchemaDOMParser", false, SchemaDOMParserTest.class.getClassLoader());
        check(!c.isInterface() && !c.isEnum(), "is a class");
        check(c.getSuperclass().getName().equals("com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler"), "superclass com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("startDocument"), "method startDocument");
        check(mn.contains("endDocument"), "method endDocument");
        check(mn.contains("comment"), "method comment");
        check(mn.contains("processingInstruction"), "method processingInstruction");
        check(mn.contains("characters"), "method characters");
        check(mn.contains("startElement"), "method startElement");
        check(mn.contains("emptyElement"), "method emptyElement");
        check(mn.contains("endElement"), "method endElement");
        check(mn.contains("ignorableWhitespace"), "method ignorableWhitespace");
        check(mn.contains("startCDATA"), "method startCDATA");
        check(mn.contains("endCDATA"), "method endCDATA");
        check(mn.contains("getDocument"), "method getDocument");
        check(mn.contains("setFeature"), "method setFeature");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("setProperty"), "method setProperty");
        check(mn.contains("getProperty"), "method getProperty");
        check(mn.contains("setEntityResolver"), "method setEntityResolver");
        check(mn.contains("parse"), "method parse");
        check(mn.contains("reset"), "method reset");
        check(mn.contains("resetNodePool"), "method resetNodePool");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
