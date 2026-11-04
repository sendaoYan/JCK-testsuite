/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO conformance: reflection.
 * @run main DOM2TOTest
 */

public class DOM2TOTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO", false, DOM2TOTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("DOM2TO"), "simple name DOM2TO");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 2, "interfaces count 2");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.xml.sax.XMLReader"), "interface org.xml.sax.XMLReader");
        check(java.util.Arrays.asList(c.getInterfaces()).toString().contains("org.xml.sax.ext.Locator2"), "interface org.xml.sax.ext.Locator2");
        check(has(c, "getContentHandler"), "method getContentHandler");
        check(has(c, "setContentHandler"), "method setContentHandler");
        check(has(c, "parse"), "method parse");
        check(has(c, "getDTDHandler"), "method getDTDHandler");
        check(has(c, "getErrorHandler"), "method getErrorHandler");
        check(has(c, "getFeature"), "method getFeature");
        check(has(c, "setFeature"), "method setFeature");
        check(has(c, "setDTDHandler"), "method setDTDHandler");
        check(has(c, "setEntityResolver"), "method setEntityResolver");
        check(has(c, "getEntityResolver"), "method getEntityResolver");
        check(has(c, "setErrorHandler"), "method setErrorHandler");
        check(has(c, "setProperty"), "method setProperty");
        check(has(c, "getProperty"), "method getProperty");
        check(has(c, "getColumnNumber"), "method getColumnNumber");
        check(has(c, "getLineNumber"), "method getLineNumber");
        check(has(c, "getPublicId"), "method getPublicId");
        check(has(c, "getSystemId"), "method getSystemId");
        check(has(c, "getXMLVersion"), "method getXMLVersion");
        check(has(c, "getEncoding"), "method getEncoding");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
