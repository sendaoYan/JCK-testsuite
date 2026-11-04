/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.runtime.output.TransletOutputHandlerFactory conformance: reflection.
 * @run main TransletOutputHandlerFactoryTest
 */

public class TransletOutputHandlerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.runtime.output.TransletOutputHandlerFactory", false, TransletOutputHandlerFactoryTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("TransletOutputHandlerFactory"), "simple name TransletOutputHandlerFactory");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "newInstance"), "method newInstance");
        check(has(c, "setOutputType"), "method setOutputType");
        check(has(c, "setEncoding"), "method setEncoding");
        check(has(c, "setOutputMethod"), "method setOutputMethod");
        check(has(c, "setOutputStream"), "method setOutputStream");
        check(has(c, "setWriter"), "method setWriter");
        check(has(c, "setHandler"), "method setHandler");
        check(has(c, "setLexicalHandler"), "method setLexicalHandler");
        check(has(c, "setNode"), "method setNode");
        check(has(c, "getNode"), "method getNode");
        check(has(c, "setNextSibling"), "method setNextSibling");
        check(has(c, "getXMLEventWriter"), "method getXMLEventWriter");
        check(has(c, "setXMLEventWriter"), "method setXMLEventWriter");
        check(has(c, "getXMLStreamWriter"), "method getXMLStreamWriter");
        check(has(c, "setXMLStreamWriter"), "method setXMLStreamWriter");
        check(has(c, "setIndentNumber"), "method setIndentNumber");
        check(has(c, "getSerializationHandler"), "method getSerializationHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
