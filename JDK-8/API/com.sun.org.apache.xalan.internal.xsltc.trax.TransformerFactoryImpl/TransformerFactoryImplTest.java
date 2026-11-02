/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl conformance: reflection.
 * @run main TransformerFactoryImplTest
 */

public class TransformerFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", false, TransformerFactoryImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("TransformerFactoryImpl"), "simple name TransformerFactoryImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.xml.transform.sax.SAXTransformerFactory"), "superclass javax.xml.transform.sax.SAXTransformerFactory");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("com.sun.org.apache.xalan.internal.xsltc.compiler.SourceLoader"), "interface com.sun.org.apache.xalan.internal.xsltc.compiler.SourceLoader");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("javax.xml.transform.ErrorListener"), "interface javax.xml.transform.ErrorListener");
        check(has(c, "getExternalExtensionsMap"), "method getExternalExtensionsMap");
        check(has(c, "setErrorListener"), "method setErrorListener");
        check(has(c, "getErrorListener"), "method getErrorListener");
        check(has(c, "getAttribute"), "method getAttribute");
        check(has(c, "setAttribute"), "method setAttribute");
        check(has(c, "setFeature"), "method setFeature");
        check(has(c, "getFeature"), "method getFeature");
        check(has(c, "overrideDefaultParser"), "method overrideDefaultParser");
        check(has(c, "getJdkXmlFeatures"), "method getJdkXmlFeatures");
        check(has(c, "getURIResolver"), "method getURIResolver");
        check(has(c, "setURIResolver"), "method setURIResolver");
        check(has(c, "getAssociatedStylesheet"), "method getAssociatedStylesheet");
        check(has(c, "newTransformer"), "method newTransformer");
        check(has(c, "newTemplates"), "method newTemplates");
        check(has(c, "newTemplatesHandler"), "method newTemplatesHandler");
        check(has(c, "newTransformerHandler"), "method newTransformerHandler");
        check(has(c, "newXMLFilter"), "method newXMLFilter");
        check(has(c, "error"), "method error");
        check(has(c, "fatalError"), "method fatalError");
        check(has(c, "warning"), "method warning");
        check(has(c, "loadSource"), "method loadSource");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
