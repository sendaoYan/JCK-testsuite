/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.SmartTransformerFactoryImpl conformance: reflection.
 * @run main SmartTransformerFactoryImplTest
 */

public class SmartTransformerFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.SmartTransformerFactoryImpl", false, SmartTransformerFactoryImplTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("SmartTransformerFactoryImpl"), "simple name SmartTransformerFactoryImpl");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.xml.transform.sax.SAXTransformerFactory"), "superclass javax.xml.transform.sax.SAXTransformerFactory");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "setErrorListener"), "method setErrorListener");
        check(has(c, "getErrorListener"), "method getErrorListener");
        check(has(c, "getAttribute"), "method getAttribute");
        check(has(c, "setAttribute"), "method setAttribute");
        check(has(c, "setFeature"), "method setFeature");
        check(has(c, "getFeature"), "method getFeature");
        check(has(c, "getURIResolver"), "method getURIResolver");
        check(has(c, "setURIResolver"), "method setURIResolver");
        check(has(c, "getAssociatedStylesheet"), "method getAssociatedStylesheet");
        check(has(c, "newTransformer"), "method newTransformer");
        check(has(c, "newTemplates"), "method newTemplates");
        check(has(c, "newTemplatesHandler"), "method newTemplatesHandler");
        check(has(c, "newTransformerHandler"), "method newTransformerHandler");
        check(has(c, "newXMLFilter"), "method newXMLFilter");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
