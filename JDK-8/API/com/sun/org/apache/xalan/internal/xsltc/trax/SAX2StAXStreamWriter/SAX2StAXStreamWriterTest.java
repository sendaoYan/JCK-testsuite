/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXStreamWriter conformance: reflection.
 * @run main SAX2StAXStreamWriterTest
 */

public class SAX2StAXStreamWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXStreamWriter", false, SAX2StAXStreamWriterTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("SAX2StAXStreamWriter"), "simple name SAX2StAXStreamWriter");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter"), "superclass com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getStreamWriter"), "method getStreamWriter");
        check(has(c, "setStreamWriter"), "method setStreamWriter");
        check(has(c, "startDocument"), "method startDocument");
        check(has(c, "endDocument"), "method endDocument");
        check(has(c, "startElement"), "method startElement");
        check(has(c, "endElement"), "method endElement");
        check(has(c, "comment"), "method comment");
        check(has(c, "characters"), "method characters");
        check(has(c, "endCDATA"), "method endCDATA");
        check(has(c, "ignorableWhitespace"), "method ignorableWhitespace");
        check(has(c, "processingInstruction"), "method processingInstruction");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
