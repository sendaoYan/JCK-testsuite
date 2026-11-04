/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXEventWriter conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.trax:+open
 * @run main SAX2StAXEventWriterTest
 */
public class SAX2StAXEventWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXEventWriter", false, SAX2StAXEventWriterTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXEventWriter"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.apache.xalan.internal.xsltc.trax.SAX2StAXBaseWriter"), "superclass");
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
        check(md.contains("characters"), "method-characters");
        check(md.contains("comment"), "method-comment");
        check(md.contains("createNamespace"), "method-createNamespace");
        check(md.contains("createStartEvents"), "method-createStartEvents");
        check(md.contains("endCDATA"), "method-endCDATA");
        check(md.contains("endDocument"), "method-endDocument");
        check(md.contains("endElement"), "method-endElement");
        check(md.contains("getEventFactory"), "method-getEventFactory");
        check(md.contains("getEventWriter"), "method-getEventWriter");
        check(md.contains("ignorableWhitespace"), "method-ignorableWhitespace");
        check(md.contains("processingInstruction"), "method-processingInstruction");
        check(md.contains("setEventFactory"), "method-setEventFactory");
        check(md.contains("setEventWriter"), "method-setEventWriter");
        check(md.contains("startDocument"), "method-startDocument");
        check(md.contains("startElement"), "method-startElement");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
