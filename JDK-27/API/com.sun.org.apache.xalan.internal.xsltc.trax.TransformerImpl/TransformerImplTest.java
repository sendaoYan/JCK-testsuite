/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl conformance: internal package reflection.
 * @modules java.xml/com.sun.org.apache.xalan.internal.xsltc.trax:+open
 * @run main TransformerImplTest
 */
public class TransformerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl", false, TransformerImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.xml.transform.Transformer"), "superclass");
        java.util.Set<String> ifs = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifs.add(i.getName());
        check(ifs.size() == 1, "interface-count");
        check(ifs.contains("com.sun.org.apache.xalan.internal.xsltc.DOMCache"), "if-com.sun.org.apache.xalan.internal.xsltc.DOMCache");
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
        check(fd.size() == 1, "field-count");
        check(fd.contains("_securityManager"), "field-_securityManager");
        check(md.size() == 25, "method-count");
        check(md.contains("clearParameters"), "method-clearParameters");
        check(md.contains("getErrorListener"), "method-getErrorListener");
        check(md.contains("getOutputHandler"), "method-getOutputHandler");
        check(md.contains("getOutputProperties"), "method-getOutputProperties");
        check(md.contains("getOutputProperty"), "method-getOutputProperty");
        check(md.contains("getParameter"), "method-getParameter");
        check(md.contains("getTransformerFactory"), "method-getTransformerFactory");
        check(md.contains("getTranslet"), "method-getTranslet");
        check(md.contains("getTransletOutputHandlerFactory"), "method-getTransletOutputHandlerFactory");
        check(md.contains("getURIResolver"), "method-getURIResolver");
        check(md.contains("isIdentity"), "method-isIdentity");
        check(md.contains("isSecureProcessing"), "method-isSecureProcessing");
        check(md.contains("overrideDefaultParser"), "method-overrideDefaultParser");
        check(md.contains("reset"), "method-reset");
        check(md.contains("retrieveDocument"), "method-retrieveDocument");
        check(md.contains("setDOM"), "method-setDOM");
        check(md.contains("setErrorListener"), "method-setErrorListener");
        check(md.contains("setOutputProperties"), "method-setOutputProperties");
        check(md.contains("setOutputProperty"), "method-setOutputProperty");
        check(md.contains("setOverrideDefaultParser"), "method-setOverrideDefaultParser");
        check(md.contains("setParameter"), "method-setParameter");
        check(md.contains("setSecureProcessing"), "method-setSecureProcessing");
        check(md.contains("setURIResolver"), "method-setURIResolver");
        check(md.contains("transferOutputProperties"), "method-transferOutputProperties");
        check(md.contains("transform"), "method-transform");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
