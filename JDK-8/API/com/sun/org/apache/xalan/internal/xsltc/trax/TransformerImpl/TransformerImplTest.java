/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl conformance: reflection.
 * @run main TransformerImplTest
 */

public class TransformerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl", false, TransformerImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("TransformerImpl"), "simple name TransformerImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "is final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.xml.transform.Transformer"), "superclass javax.xml.transform.Transformer");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.DOMCache"), "interface com.sun.org.apache.xalan.internal.xsltc.DOMCache");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("javax.xml.transform.ErrorListener"), "interface javax.xml.transform.ErrorListener");
        check(has(c, "isSecureProcessing"), "method isSecureProcessing");
        check(has(c, "setSecureProcessing"), "method setSecureProcessing");
        check(has(c, "overrideDefaultParser"), "method overrideDefaultParser");
        check(has(c, "setOverrideDefaultParser"), "method setOverrideDefaultParser");
        check(has(c, "isIdentity"), "method isIdentity");
        check(has(c, "transform"), "method transform");
        check(has(c, "getOutputHandler"), "method getOutputHandler");
        check(has(c, "getErrorListener"), "method getErrorListener");
        check(has(c, "setErrorListener"), "method setErrorListener");
        check(has(c, "getOutputProperties"), "method getOutputProperties");
        check(has(c, "getOutputProperty"), "method getOutputProperty");
        check(has(c, "setOutputProperties"), "method setOutputProperties");
        check(has(c, "setOutputProperty"), "method setOutputProperty");
        check(has(c, "transferOutputProperties"), "method transferOutputProperties");
        check(has(c, "setParameter"), "method setParameter");
        check(has(c, "clearParameters"), "method clearParameters");
        check(has(c, "getParameter"), "method getParameter");
        check(has(c, "getURIResolver"), "method getURIResolver");
        check(has(c, "setURIResolver"), "method setURIResolver");
        check(has(c, "retrieveDocument"), "method retrieveDocument");
        check(has(c, "error"), "method error");
        check(has(c, "fatalError"), "method fatalError");
        check(has(c, "warning"), "method warning");
        check(has(c, "reset"), "method reset");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
