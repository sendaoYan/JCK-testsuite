/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.runtime.output.TransletOutputHandlerFactory conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.runtime.output:+open
 * @run main TransletOutputHandlerFactoryTest
 */
public class TransletOutputHandlerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.runtime.output.TransletOutputHandlerFactory", false, TransletOutputHandlerFactoryTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.runtime.output.TransletOutputHandlerFactory"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
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
        check(fd.size() == 4, "field-count");
        check(fd.contains("STREAM"), "field-STREAM");
        check(fd.contains("SAX"), "field-SAX");
        check(fd.contains("DOM"), "field-DOM");
        check(fd.contains("STAX"), "field-STAX");
        check(md.size() == 17, "method-count");
        check(md.contains("getNode"), "method-getNode");
        check(md.contains("getSerializationHandler"), "method-getSerializationHandler");
        check(md.contains("getXMLEventWriter"), "method-getXMLEventWriter");
        check(md.contains("getXMLStreamWriter"), "method-getXMLStreamWriter");
        check(md.contains("newInstance"), "method-newInstance");
        check(md.contains("setEncoding"), "method-setEncoding");
        check(md.contains("setHandler"), "method-setHandler");
        check(md.contains("setIndentNumber"), "method-setIndentNumber");
        check(md.contains("setLexicalHandler"), "method-setLexicalHandler");
        check(md.contains("setNextSibling"), "method-setNextSibling");
        check(md.contains("setNode"), "method-setNode");
        check(md.contains("setOutputMethod"), "method-setOutputMethod");
        check(md.contains("setOutputStream"), "method-setOutputStream");
        check(md.contains("setOutputType"), "method-setOutputType");
        check(md.contains("setWriter"), "method-setWriter");
        check(md.contains("setXMLEventWriter"), "method-setXMLEventWriter");
        check(md.contains("setXMLStreamWriter"), "method-setXMLStreamWriter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
