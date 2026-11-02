/*
 * @test
 * @summary com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter conformance: reflection.
 * @run main TrAXFilterTest
 */

public class TrAXFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter", false, TrAXFilterTest.class.getClassLoader());
        check(c != null, "class loads");
        check(c.getSimpleName().equals("TrAXFilter"), "simple name TrAXFilter");
        check(!c.isInterface(), "is class");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.xml.sax.helpers.XMLFilterImpl"), "superclass org.xml.sax.helpers.XMLFilterImpl");
        check(c.getInterfaces().length == 0, "interfaces count 0");
        check(has(c, "getTransformer"), "method getTransformer");
        check(has(c, "parse"), "method parse");
        check(has(c, "setContentHandler"), "method setContentHandler");
        check(has(c, "setErrorListener"), "method setErrorListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean has(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
}
